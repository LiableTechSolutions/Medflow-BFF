package com.medflow.modules.patients;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
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
 * Boots the full application (Flyway migrations, security chain) against in-memory H2, the
 * same way {@code MedflowSmokeTest} does, and walks the patient hospitalisation feature:
 * a normal OPD registration stays untouched by the hospitalisation tables, a patient can be
 * created already admitted, an existing OPD patient can be admitted afterwards, only a
 * doctor may record a daily analysis against an active stay, and discharging preserves
 * history while flipping the patient back to OPD.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:medflow_hospitalisation;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "medflow.cache.provider=simple"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientHospitalisationTest {

  private static final String DOCTOR_PASSWORD = "DoctorPass123!";

  @Autowired
  private TestRestTemplate rest;

  private static String adminToken;
  private static String doctorToken;
  private static Integer doctorId;
  private static Integer opdPatientId;
  private static Integer hospitalisedPatientId;
  private static Integer hospitalisationRecordId;

  @Test
  @Order(1)
  void setUpWorkspaceAndDoctor() {
    var register = rest.postForEntity("/api/v1/auth/register", json(Map.of(
        "fullName", "Priya Nair",
        "email", "founder@hospitaltrack.local",
        "password", "changeit-123",
        "confirmPassword", "changeit-123",
        "hospitalName", "Hospitalisation Test Clinic")), String.class);
    assertThat(register.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    adminToken = JsonPath.read(register.getBody(), "$.data.accessToken");

    var doctor = rest.exchange("/api/v1/doctors", HttpMethod.POST, authorized(adminToken, Map.of(
        "firstName", "Rohan",
        "lastName", "Verma",
        "email", "rohan.verma@hospitaltrack.local",
        "password", DOCTOR_PASSWORD,
        "specialty", "Internal Medicine",
        "qualification", "MBBS, MD",
        "registrationNumber", "REG-777001",
        "yearsOfExperience", 8,
        "consultationFee", 800.00)), String.class);
    assertThat(doctor.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    doctorId = JsonPath.read(doctor.getBody(), "$.data.id");

    var login = rest.postForEntity("/api/v1/auth/login", json(Map.of(
        "email", "rohan.verma@hospitaltrack.local",
        "password", DOCTOR_PASSWORD)), String.class);
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    doctorToken = JsonPath.read(login.getBody(), "$.data.accessToken");
  }

  @Test
  @Order(2)
  void registeringAPatientDefaultsToOpd() {
    var response = rest.exchange("/api/v1/patients", HttpMethod.POST, authorized(adminToken, Map.of(
        "firstName", "Anita",
        "lastName", "Kumar",
        "dateOfBirth", "1985-06-20",
        "gender", "FEMALE",
        "email", "anita.kumar@hospitaltrack.local",
        "bloodGroup", "B+")), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<Boolean>read(response.getBody(), "$.data.isHospitalised")).isFalse();
    opdPatientId = JsonPath.read(response.getBody(), "$.data.id");

    var summary = rest.exchange("/api/v1/patients/" + opdPatientId + "/summary", HttpMethod.GET,
        authorized(adminToken, null), String.class);
    assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<Boolean>read(summary.getBody(), "$.data.isHospitalised")).isFalse();
    // Jackson is configured with non-null inclusion, so a null hospitalisation is omitted
    // from the payload entirely rather than serialized as "currentHospitalisation": null.
    Map<String, Object> data = JsonPath.read(summary.getBody(), "$.data");
    assertThat(data).doesNotContainKey("currentHospitalisation");
    assertThat(JsonPath.<List<?>>read(summary.getBody(), "$.data.dailyAnalyses")).isEmpty();
  }

  @Test
  @Order(3)
  void patientCanBeCreatedAlreadyHospitalised() {
    var body = new HashMap<String, Object>();
    body.put("firstName", "Vikram");
    body.put("lastName", "Singh");
    body.put("dateOfBirth", "1978-01-15");
    body.put("gender", "MALE");
    body.put("email", "vikram.singh@hospitaltrack.local");
    body.put("bloodGroup", "O+");
    body.put("isHospitalised", true);
    body.put("hospitalisation", Map.of(
        "admittingDoctorId", doctorId,
        "ward", "ICU",
        "bed", "A1"));

    var response = rest.exchange("/api/v1/patients", HttpMethod.POST, authorized(adminToken, body),
        String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<Boolean>read(response.getBody(), "$.data.isHospitalised")).isTrue();
    hospitalisedPatientId = JsonPath.read(response.getBody(), "$.data.id");

    var summary = rest.exchange("/api/v1/patients/" + hospitalisedPatientId + "/summary",
        HttpMethod.GET, authorized(adminToken, null), String.class);
    assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<Boolean>read(summary.getBody(), "$.data.isHospitalised")).isTrue();
    assertThat(JsonPath.<String>read(summary.getBody(), "$.data.currentHospitalisation.status"))
        .isEqualTo("ADMITTED");
    assertThat(JsonPath.<String>read(summary.getBody(), "$.data.currentHospitalisation.ward"))
        .isEqualTo("ICU");
    hospitalisationRecordId = JsonPath.read(summary.getBody(),
        "$.data.currentHospitalisation.id");
  }

  @Test
  @Order(4)
  void creatingAHospitalisedPatientWithoutAdmissionDetailsIsRejected() {
    var body = new HashMap<String, Object>();
    body.put("firstName", "Missing");
    body.put("lastName", "Details");
    body.put("email", "missing.details@hospitaltrack.local");
    body.put("isHospitalised", true);

    var response = rest.exchange("/api/v1/patients", HttpMethod.POST, authorized(adminToken, body),
        String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @Test
  @Order(5)
  void anExistingOpdPatientCanBeAdmitted() {
    var response = rest.exchange("/api/v1/patients/" + opdPatientId + "/hospitalisation",
        HttpMethod.POST, authorized(adminToken, Map.of(
            "admittingDoctorId", doctorId,
            "ward", "General Ward",
            "bed", "B3")), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.status")).isEqualTo("ADMITTED");

    var patient = rest.exchange("/api/v1/patients/" + opdPatientId, HttpMethod.GET,
        authorized(adminToken, null), String.class);
    assertThat(JsonPath.<Boolean>read(patient.getBody(), "$.data.isHospitalised")).isTrue();
  }

  @Test
  @Order(6)
  void onlyADoctorCanRecordADailyAnalysis() {
    Map<String, Object> body = Map.of(
        "recordedByDoctorId", doctorId,
        "bloodPressure", "120/80",
        "pulse", 78,
        "temperature", 98.6,
        "spo2", 97,
        "notes", "Stable overnight");

    var asAdmin = rest.exchange(
        "/api/v1/patients/" + hospitalisedPatientId + "/hospitalisation/daily-analyses",
        HttpMethod.POST, authorized(adminToken, body), String.class);
    assertThat(asAdmin.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    var asDoctor = rest.exchange(
        "/api/v1/patients/" + hospitalisedPatientId + "/hospitalisation/daily-analyses",
        HttpMethod.POST, authorized(doctorToken, body), String.class);
    assertThat(asDoctor.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(JsonPath.<Integer>read(asDoctor.getBody(), "$.data.recordedByDoctorId"))
        .isEqualTo(doctorId);
    assertThat(JsonPath.<Integer>read(asDoctor.getBody(), "$.data.hospitalisationRecordId"))
        .isEqualTo(hospitalisationRecordId);

    // A second entry so the list assertion below proves the ordering, not just the count.
    Map<String, Object> second = Map.of("recordedByDoctorId", doctorId, "bloodPressure", "118/76",
        "pulse", 74, "notes", "Improving");
    var secondEntry = rest.exchange(
        "/api/v1/patients/" + hospitalisedPatientId + "/hospitalisation/daily-analyses",
        HttpMethod.POST, authorized(doctorToken, second), String.class);
    assertThat(secondEntry.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var list = rest.exchange(
        "/api/v1/patients/" + hospitalisedPatientId + "/hospitalisation/daily-analyses",
        HttpMethod.GET, authorized(adminToken, null), String.class);
    assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
    var notes = JsonPath.<List<String>>read(list.getBody(), "$.data[*].notes");
    assertThat(notes).containsExactly("Stable overnight", "Improving");

    var summary = rest.exchange("/api/v1/patients/" + hospitalisedPatientId + "/summary",
        HttpMethod.GET, authorized(adminToken, null), String.class);
    assertThat(JsonPath.<List<?>>read(summary.getBody(), "$.data.dailyAnalyses")).hasSize(2);
  }

  @Test
  @Order(7)
  void dailyAnalysisIsRejectedWithoutAnActiveHospitalisation() {
    Map<String, Object> body = Map.of("recordedByDoctorId", doctorId, "notes", "Should be rejected");
    // opdPatientId's own stay from test 5 is active, so use a fresh OPD-only patient instead.
    var response = rest.exchange("/api/v1/patients", HttpMethod.POST, authorized(adminToken, Map.of(
        "firstName", "Neha",
        "lastName", "Desai",
        "email", "neha.desai@hospitaltrack.local")), String.class);
    Integer neverAdmittedPatientId = JsonPath.read(response.getBody(), "$.data.id");

    var rejected = rest.exchange(
        "/api/v1/patients/" + neverAdmittedPatientId + "/hospitalisation/daily-analyses",
        HttpMethod.POST, authorized(doctorToken, body), String.class);
    assertThat(rejected.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @Test
  @Order(8)
  void dischargingClosesTheStayWithoutDeletingHistory() {
    var response = rest.exchange(
        "/api/v1/patients/" + hospitalisedPatientId + "/hospitalisation/discharge",
        HttpMethod.PATCH, authorized(adminToken, Map.of()), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.status")).isEqualTo("DISCHARGED");
    assertThat((Object) JsonPath.read(response.getBody(), "$.data.dischargeDate")).isNotNull();

    var patient = rest.exchange("/api/v1/patients/" + hospitalisedPatientId, HttpMethod.GET,
        authorized(adminToken, null), String.class);
    assertThat(JsonPath.<Boolean>read(patient.getBody(), "$.data.isHospitalised")).isFalse();

    // History is preserved: the summary still surfaces the (now discharged) stay and its
    // daily analyses instead of hiding them.
    var summary = rest.exchange("/api/v1/patients/" + hospitalisedPatientId + "/summary",
        HttpMethod.GET, authorized(adminToken, null), String.class);
    assertThat(JsonPath.<String>read(summary.getBody(), "$.data.currentHospitalisation.status"))
        .isEqualTo("DISCHARGED");
    assertThat(JsonPath.<List<?>>read(summary.getBody(), "$.data.dailyAnalyses")).hasSize(2);

    var dischargedAgain = rest.exchange(
        "/api/v1/patients/" + hospitalisedPatientId + "/hospitalisation/discharge",
        HttpMethod.PATCH, authorized(adminToken, Map.of()), String.class);
    assertThat(dischargedAgain.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  private HttpEntity<Map<String, Object>> authorized(String token, Map<String, Object> body) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
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
