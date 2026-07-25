package com.medflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Boots the entire application (Flyway migrations, JPA validation, security chain,
 * Modulith event listeners) against in-memory H2 in PostgreSQL mode and walks the
 * cross-module happy path: register → onboard doctor → register patient → book
 * appointment → notification raised → dashboard KPIs reflect the data. Redis is
 * intentionally absent to prove the cache degrades gracefully.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:medflow;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.data.redis.timeout=200ms",
        "spring.data.redis.connect-timeout=200ms"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MedflowSmokeTest {

  @Autowired
  private TestRestTemplate rest;

  private static String accessToken;
  private static String doctorId;
  private static String patientId;

  @Test
  @Order(1)
  void healthEndpointIsPublic() {
    var response = rest.getForEntity("/health", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<Boolean>read(response.getBody(), "$.success")).isTrue();
  }

  @Test
  @Order(2)
  void protectedEndpointsRejectAnonymousCalls() {
    var response = rest.getForEntity("/api/v1/patients", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @Order(3)
  void firstRegisteredUserBecomesWorkspaceAdmin() {
    var response = rest.postForEntity("/api/v1/auth/register", json(Map.of(
        "fullName", "Dr. Ananya Rao",
        "email", "ananya@medflow.local",
        "password", "changeit-123",
        "confirmPassword", "changeit-123")), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.user.role")).isEqualTo("ADMIN");
    accessToken = JsonPath.read(response.getBody(), "$.data.accessToken");
    assertThat(accessToken).isNotBlank();
  }

  @Test
  @Order(4)
  void loginIssuesBearerToken() {
    var response = rest.postForEntity("/api/v1/auth/login", json(Map.of(
        "email", "ananya@medflow.local",
        "password", "changeit-123")), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.tokenType")).isEqualTo("Bearer");
  }

  @Test
  @Order(5)
  void adminOnboardsDoctorAndRegistersPatient() {
    var doctor = rest.exchange("/api/v1/doctors", HttpMethod.POST, authorized(Map.of(
        "fullName", "Dr. Kabir Shah",
        "email", "kabir@medflow.local",
        "specialty", "Cardiology",
        "department", "Cardiology",
        "licenseNumber", "MED-12345",
        "consultationFee", 1500.00)), String.class);
    assertThat(doctor.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    doctorId = JsonPath.read(doctor.getBody(), "$.data.id");

    var patient = rest.exchange("/api/v1/patients", HttpMethod.POST, authorized(Map.of(
        "firstName", "Meera",
        "lastName", "Joshi",
        "dateOfBirth", "1991-04-12",
        "gender", "FEMALE",
        "email", "meera@example.com",
        "bloodGroup", "O+")), String.class);
    assertThat(patient.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    patientId = JsonPath.read(patient.getBody(), "$.data.id");
  }

  @Test
  @Order(6)
  void bookingAnAppointmentRaisesANotification() {
    var response = rest.exchange("/api/v1/appointments", HttpMethod.POST, authorized(Map.of(
        "patientId", patientId,
        "doctorId", doctorId,
        "scheduledAt", Instant.now().plus(Duration.ofDays(1)).toString(),
        "reason", "Cardiology review")), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.patientName"))
        .isEqualTo("Meera Joshi");
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.status")).isEqualTo("PENDING");

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      var unread = rest.exchange("/api/v1/notifications/unread-count", HttpMethod.GET,
          authorized(null), String.class);
      assertThat(unread.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(JsonPath.<Integer>read(unread.getBody(), "$.data.unread")).isGreaterThan(0);
    });
  }

  @Test
  @Order(7)
  void dashboardAggregatesSurviveMissingRedis() {
    var response = rest.exchange("/api/v1/analytics/dashboard", HttpMethod.GET, authorized(null),
        String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.totalPatients")).isEqualTo(1);
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.doctorsOnStaff")).isEqualTo(1);
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.appointmentsToday")).isZero();
  }

  private HttpEntity<Map<String, Object>> authorized(Map<String, Object> body) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    return new HttpEntity<>(body, headers);
  }

  private HttpEntity<Map<String, Object>> json(Map<String, Object> body) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }
}
