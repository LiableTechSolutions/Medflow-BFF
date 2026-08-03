package com.medflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
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
 * cross-module happy path: register a workspace → onboard a doctor → register a patient →
 * book an appointment → an event produces a notification → the dashboard reflects it.
 *
 * <p>It also proves tenant isolation: the freshly created workspace sees only its own
 * records, never the demo hospital seeded from {@code db/demo}.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:medflow;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "medflow.cache.provider=simple"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MedflowSmokeTest {

  @Autowired
  private TestRestTemplate rest;

  private static String accessToken;
  private static Integer doctorId;
  private static Integer patientId;

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
  void signingUpCreatesAWorkspaceWithAnAdministrator() {
    var response = rest.postForEntity("/api/v1/auth/register", json(Map.of(
        "fullName", "Ananya Rao",
        "email", "founder@newclinic.local",
        "password", "changeit-123",
        "confirmPassword", "changeit-123",
        "hospitalName", "New Clinic")), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.user.roleCode")).isEqualTo("ADMIN");
    assertThat(JsonPath.<List<String>>read(response.getBody(), "$.data.permissions"))
        .contains("users:write");
    accessToken = JsonPath.read(response.getBody(), "$.data.accessToken");
    assertThat(accessToken).isNotBlank();
  }

  @Test
  @Order(4)
  void loginIssuesBearerToken() {
    var response = rest.postForEntity("/api/v1/auth/login", json(Map.of(
        "email", "founder@newclinic.local",
        "password", "changeit-123")), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.tokenType")).isEqualTo("Bearer");
  }

  @Test
  @Order(5)
  void newWorkspaceIsEntitledToThePhaseOneModules() {
    var response = rest.exchange("/api/v1/hospital/modules", HttpMethod.GET, authorized(null),
        String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<List<String>>read(response.getBody(),
        "$.data[?(@.accessible == true)].moduleCode")).contains("appointments", "patients");
  }

  @Test
  @Order(6)
  void adminOnboardsDoctorAndRegistersPatient() {
    var doctor = rest.exchange("/api/v1/doctors", HttpMethod.POST, authorized(Map.of(
        "firstName", "Kabir",
        "lastName", "Shah",
        "email", "kabir@newclinic.local",
        "specialty", "Cardiology",
        "qualification", "MBBS, MD",
        "registrationNumber", "REG-999001",
        "yearsOfExperience", 12,
        "consultationFee", 1500.00)), String.class);
    assertThat(doctor.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<String>read(doctor.getBody(), "$.data.doctorCode")).isEqualTo("DOC-0001");
    doctorId = JsonPath.read(doctor.getBody(), "$.data.id");

    var patient = rest.exchange("/api/v1/patients", HttpMethod.POST, authorized(Map.of(
        "firstName", "Meera",
        "lastName", "Joshi",
        "dateOfBirth", "1991-04-12",
        "gender", "FEMALE",
        "email", "meera@newclinic.local",
        "bloodGroup", "O+")), String.class);
    assertThat(patient.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<String>read(patient.getBody(), "$.data.patientCode"))
        .isEqualTo("PAT-000001");
    patientId = JsonPath.read(patient.getBody(), "$.data.id");
  }

  @Test
  @Order(7)
  void bookingAnAppointmentAssignsAQueueNumberAndRaisesANotification() {
    var body = new HashMap<String, Object>();
    body.put("patientId", patientId);
    body.put("doctorId", doctorId);
    body.put("scheduledAt", Instant.now().plus(Duration.ofDays(1)).toString());
    body.put("appointmentMode", "WALK_IN");
    body.put("reason", "Cardiology review");

    var response = rest.exchange("/api/v1/appointments", HttpMethod.POST, authorized(body),
        String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.patientName"))
        .isEqualTo("Meera Joshi");
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.status")).isEqualTo("BOOKED");
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.queueNumber")).isEqualTo(1);

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      var unread = rest.exchange("/api/v1/notifications/unread-count", HttpMethod.GET,
          authorized(null), String.class);
      assertThat(unread.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(JsonPath.<Integer>read(unread.getBody(), "$.data.unread")).isGreaterThan(0);
    });
  }

  @Test
  @Order(8)
  void dashboardCountsOnlyTheCallersHospital() {
    var response = rest.exchange("/api/v1/analytics/dashboard", HttpMethod.GET, authorized(null),
        String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    // The demo workspace holds five patients and three doctors; this tenant has one each.
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.totalPatients")).isEqualTo(1);
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.doctorsOnStaff")).isEqualTo(1);
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.appointmentsToday")).isZero();
  }

  @Test
  @Order(9)
  void auditTrailRecordsAdministrativeActions() {
    var response = rest.exchange("/api/v1/audit-logs?entityType=doctor", HttpMethod.GET,
        authorized(null), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<List<String>>read(response.getBody(), "$.data.content[*].action"))
        .contains("DOCTOR_CREATED");
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
