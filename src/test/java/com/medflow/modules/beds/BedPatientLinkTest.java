package com.medflow.modules.beds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
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
import org.springframework.http.ResponseEntity;

/**
 * The patient side of beds: finding the bed a patient is in, listing free beds to choose
 * from, and — the important part — a discharge freeing the bed on its own, without any
 * caller remembering to. Includes the failure cases: a discharge that is refused must
 * leave the bed alone, and discharging someone with no bed must not fail.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:medflow_bed_patient;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "medflow.cache.provider=simple"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BedPatientLinkTest {

  private static final String PASSWORD = "StaffPass123!";

  @Autowired
  private TestRestTemplate rest;

  private static String adminToken;
  private static String otherHospitalToken;
  private static Integer doctorId;
  private static Integer admitted;
  private static Integer admittedNoBed;
  private static Integer notAdmitted;
  private static List<Integer> bedIds;

  @Test
  @Order(1)
  void setUp() {
    adminToken = register("admin@link-a.local", "Link Hospital A");
    otherHospitalToken = register("admin@link-b.local", "Link Hospital B");

    var doctor = call(HttpMethod.POST, "/api/v1/doctors", adminToken, Map.of(
        "firstName", "Dev", "lastName", "Doctor", "email", "dev@link-a.local",
        "password", PASSWORD, "specialty", "Internal Medicine", "qualification", "MBBS",
        "registrationNumber", "REG-LINK-1", "yearsOfExperience", 5, "consultationFee", 500.00));
    assertThat(doctor.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    doctorId = JsonPath.read(doctor.getBody(), "$.data.id");

    admitted = createPatient("Asha");
    admittedNoBed = createPatient("Bala");
    notAdmitted = createPatient("Chitra");
    admit(admitted);
    admit(admittedNoBed);

    var ward = call(HttpMethod.POST, "/api/v1/beds/wards", adminToken,
        Map.of("name", "Surgical", "bedCount", 3));
    assertThat(ward.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var wardId = JsonPath.<Integer>read(ward.getBody(), "$.data.id");
    bedIds = JsonPath.read(call(HttpMethod.GET, "/api/v1/beds/wards/" + wardId + "/beds", adminToken, null)
        .getBody(), "$.data[*].id");
  }

  @Test
  @Order(2)
  void freeBedsAreListedAcrossWards() {
    var response = call(HttpMethod.GET, "/api/v1/beds/available", adminToken, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<List<String>>read(response.getBody(), "$.data[*].bedNumber"))
        .containsExactly("Surgical-01", "Surgical-02", "Surgical-03");
  }

  @Test
  @Order(3)
  void aPatientWithoutABedHasNone() {
    var response = call(HttpMethod.GET, "/api/v1/beds/patients/" + admitted, adminToken, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).doesNotContain("\"status\"");
  }

  @Test
  @Order(4)
  void assigningABedMakesItTheirsAndRemovesItFromTheFreeList() {
    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(0) + "/assign", adminToken,
        Map.of("patientId", admitted)).getStatusCode()).isEqualTo(HttpStatus.OK);

    var mine = call(HttpMethod.GET, "/api/v1/beds/patients/" + admitted, adminToken, null);
    assertThat(JsonPath.<String>read(mine.getBody(), "$.data.bedNumber")).isEqualTo("Surgical-01");
    assertThat(JsonPath.<String>read(mine.getBody(), "$.data.status")).isEqualTo("OCCUPIED");
    assertThat(JsonPath.<String>read(mine.getBody(), "$.data.patientName")).contains("Asha");

    var free = call(HttpMethod.GET, "/api/v1/beds/available", adminToken, null);
    assertThat(JsonPath.<List<String>>read(free.getBody(), "$.data[*].bedNumber"))
        .containsExactly("Surgical-02", "Surgical-03");
  }

  @Test
  @Order(5)
  void anotherHospitalCannotSeeThisPatientsBed() {
    var response = call(HttpMethod.GET, "/api/v1/beds/patients/" + admitted, otherHospitalToken, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).doesNotContain("Surgical").doesNotContain("Asha");
    assertThat(JsonPath.<List<?>>read(
        call(HttpMethod.GET, "/api/v1/beds/available", otherHospitalToken, null).getBody(), "$.data"))
        .isEmpty();
  }

  @Test
  @Order(6)
  void aRefusedDischargeLeavesTheBedAlone() {
    // Patient has a bed but was never admitted, so there is no stay to discharge.
    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(1) + "/assign", adminToken,
        Map.of("patientId", notAdmitted)).getStatusCode()).isEqualTo(HttpStatus.OK);

    var discharge = call(HttpMethod.PATCH,
        "/api/v1/patients/" + notAdmitted + "/hospitalisation/discharge", adminToken, Map.of());
    assertThat(discharge.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

    var bed = call(HttpMethod.GET, "/api/v1/beds/patients/" + notAdmitted, adminToken, null);
    assertThat(JsonPath.<String>read(bed.getBody(), "$.data.status")).isEqualTo("OCCUPIED");
  }

  @Test
  @Order(7)
  void dischargingAPatientWithoutABedStillSucceeds() {
    var discharge = call(HttpMethod.PATCH,
        "/api/v1/patients/" + admittedNoBed + "/hospitalisation/discharge", adminToken, Map.of());

    assertThat(discharge.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @Order(8)
  void dischargeFreesTheBedAutomatically() {
    var discharge = call(HttpMethod.PATCH,
        "/api/v1/patients/" + admitted + "/hospitalisation/discharge", adminToken, Map.of());
    assertThat(discharge.getStatusCode()).isEqualTo(HttpStatus.OK);

    // The release runs after the discharge commits, so give the listener a moment.
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      var mine = call(HttpMethod.GET, "/api/v1/beds/patients/" + admitted, adminToken, null);
      assertThat(mine.getBody()).doesNotContain("Surgical");
    });

    var free = call(HttpMethod.GET, "/api/v1/beds/available", adminToken, null);
    assertThat(JsonPath.<List<String>>read(free.getBody(), "$.data[*].bedNumber"))
        .contains("Surgical-01", "Surgical-03");
  }

  @Test
  @Order(9)
  void theFreedBedCanBeAssignedToSomeoneElse() {
    var response = call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(0) + "/assign", adminToken,
        Map.of("patientId", admittedNoBed));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private void admit(Integer patientId) {
    var response = call(HttpMethod.POST, "/api/v1/patients/" + patientId + "/hospitalisation",
        adminToken, Map.of("admittingDoctorId", doctorId, "ward", "Surgical"));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  private String register(String email, String hospital) {
    var response = rest.postForEntity("/api/v1/auth/register", json(Map.of(
        "fullName", "Admin User", "email", email, "password", "changeit-123",
        "confirmPassword", "changeit-123", "hospitalName", hospital)), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return JsonPath.read(response.getBody(), "$.data.accessToken");
  }

  private Integer createPatient(String firstName) {
    var response = call(HttpMethod.POST, "/api/v1/patients", adminToken, Map.of(
        "firstName", firstName, "lastName", "Patient", "dateOfBirth", "1990-04-12",
        "gender", "FEMALE", "email", firstName.toLowerCase() + "@link-a.local", "bloodGroup", "O+"));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return JsonPath.read(response.getBody(), "$.data.id");
  }

  private ResponseEntity<String> call(HttpMethod method, String path, String token,
      Map<String, Object> body) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    return rest.exchange(path, method, new HttpEntity<>(body, headers), String.class);
  }

  private HttpEntity<Map<String, Object>> json(Map<String, Object> body) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }
}
