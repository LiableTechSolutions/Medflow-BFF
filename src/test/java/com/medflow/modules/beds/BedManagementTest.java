package com.medflow.modules.beds;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
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
 * Walks bed management end to end against the real application (Flyway, security chain,
 * H2): growing and shrinking a ward, assigning and releasing patients, the role split
 * between admin / nurse / doctor, and tenant isolation between two hospitals. Each step
 * is either a positive path (it must work) or a negative one (it must be refused with the
 * right status and leave state unchanged).
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:medflow_beds;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "medflow.cache.provider=simple"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BedManagementTest {

  private static final String PASSWORD = "StaffPass123!";

  @Autowired
  private TestRestTemplate rest;

  private static String adminToken;
  private static String nurseToken;
  private static String doctorToken;
  private static String otherHospitalToken;
  private static Integer patientA;
  private static Integer patientB;
  private static Integer wardId;
  private static List<Integer> bedIds;

  @Test
  @Order(1)
  void setUpTwoHospitalsAndStaff() {
    adminToken = register("admin@beds-a.local", "Beds Hospital A");
    otherHospitalToken = register("admin@beds-b.local", "Beds Hospital B");

    var nurse = call(HttpMethod.POST, "/api/v1/users", adminToken, Map.of(
        "firstName", "Nina", "lastName", "Nurse", "email", "nina@beds-a.local",
        "rawPassword", PASSWORD, "roleCode", "NURSE"));
    assertThat(nurse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    nurseToken = login("nina@beds-a.local");

    var doctor = call(HttpMethod.POST, "/api/v1/doctors", adminToken, Map.of(
        "firstName", "Dev", "lastName", "Doctor", "email", "dev@beds-a.local",
        "password", PASSWORD, "specialty", "Internal Medicine", "qualification", "MBBS",
        "registrationNumber", "REG-BEDS-1", "yearsOfExperience", 5, "consultationFee", 500.00));
    assertThat(doctor.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    doctorToken = login("dev@beds-a.local");

    patientA = createPatient("Asha", "asha@beds-a.local");
    patientB = createPatient("Bala", "bala@beds-a.local");
  }

  @Test
  @Order(2)
  void adminCreatesAWardWithInitialBeds() {
    var response = call(HttpMethod.POST, "/api/v1/beds/wards", adminToken,
        Map.of("name", "ICU", "wardType", "CRITICAL", "bedCount", 5));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    wardId = JsonPath.read(response.getBody(), "$.data.id");
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.totalBeds")).isEqualTo(5);
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.availableBeds")).isEqualTo(5);
  }

  @Test
  @Order(3)
  void aDuplicateWardNameIsRejectedIgnoringCase() {
    var response = call(HttpMethod.POST, "/api/v1/beds/wards", adminToken,
        Map.of("name", "icu", "bedCount", 2));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  @Order(4)
  void invalidWardAndCountRequestsAreRejected() {
    assertThat(call(HttpMethod.POST, "/api/v1/beds/wards", adminToken,
        Map.of("name", "  ", "bedCount", 2)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(call(HttpMethod.POST, "/api/v1/beds/wards", adminToken,
        Map.of("name", "Huge", "bedCount", 201)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(call(HttpMethod.POST, "/api/v1/beds/wards", adminToken,
        Map.of("name", "Negative", "bedCount", -1)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(call(HttpMethod.POST, "/api/v1/beds/wards/" + wardId + "/beds/add", adminToken,
        Map.of("count", 0)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(call(HttpMethod.POST, "/api/v1/beds/wards/" + wardId + "/beds/reduce", adminToken,
        Map.of("count", 0)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @Order(5)
  void addingBedsContinuesTheNumbering() {
    var response = call(HttpMethod.POST, "/api/v1/beds/wards/" + wardId + "/beds/add", adminToken,
        Map.of("count", 3));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<Integer>read(response.getBody(), "$.data.totalBeds")).isEqualTo(8);

    var beds = call(HttpMethod.GET, "/api/v1/beds/wards/" + wardId + "/beds", adminToken, null);
    assertThat(JsonPath.<List<String>>read(beds.getBody(), "$.data[*].bedNumber"))
        .containsExactly("ICU-01", "ICU-02", "ICU-03", "ICU-04", "ICU-05", "ICU-06", "ICU-07", "ICU-08");
    bedIds = JsonPath.read(beds.getBody(), "$.data[*].id");
  }

  @Test
  @Order(6)
  void aNurseCanAssignAPatientToABed() {
    var response = call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(0) + "/assign", nurseToken,
        Map.of("patientId", patientA));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.status")).isEqualTo("OCCUPIED");
    assertThat(JsonPath.<String>read(response.getBody(), "$.data.patientName")).contains("Asha");
  }

  @Test
  @Order(7)
  void assigningIsRefusedWhenTheBedIsTakenThePatientAlreadyHasOneOrTheyDontExist() {
    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(0) + "/assign", nurseToken,
        Map.of("patientId", patientB)).getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(1) + "/assign", nurseToken,
        Map.of("patientId", patientA)).getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(1) + "/assign", nurseToken,
        Map.of("patientId", 999_999)).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @Order(8)
  void rolesAreEnforced() {
    var path = "/api/v1/beds/wards/" + wardId + "/beds/";
    // A doctor is neither admin nor ward staff.
    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(2) + "/assign", doctorToken,
        Map.of("patientId", patientB)).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(call(HttpMethod.POST, path + "add", doctorToken, Map.of("count", 1)).getStatusCode())
        .isEqualTo(HttpStatus.FORBIDDEN);
    // A nurse manages occupancy but not capacity.
    assertThat(call(HttpMethod.POST, path + "add", nurseToken, Map.of("count", 1)).getStatusCode())
        .isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(call(HttpMethod.POST, path + "reduce", nurseToken, Map.of("count", 1)).getStatusCode())
        .isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(call(HttpMethod.POST, "/api/v1/beds/wards", nurseToken,
        Map.of("name", "Nope", "bedCount", 1)).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    // Reading is open to any signed-in staff.
    assertThat(call(HttpMethod.GET, "/api/v1/beds/wards", doctorToken, null).getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  @Order(9)
  void maintenanceBlocksAssignmentAndCannotHitAnOccupiedBed() {
    var maintain = call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(1) + "/maintenance",
        nurseToken, Map.of("underMaintenance", true));
    assertThat(maintain.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<String>read(maintain.getBody(), "$.data.status")).isEqualTo("MAINTENANCE");

    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(1) + "/assign", nurseToken,
        Map.of("patientId", patientB)).getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(0) + "/maintenance", nurseToken,
        Map.of("underMaintenance", true)).getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @Test
  @Order(10)
  void reducingNeverRemovesOccupiedOrMaintenanceBeds() {
    // 8 beds: 1 occupied, 1 under maintenance, 6 free.
    var tooMany = call(HttpMethod.POST, "/api/v1/beds/wards/" + wardId + "/beds/reduce", adminToken,
        Map.of("count", 7));
    assertThat(tooMany.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(wardTotal()).isEqualTo(8);

    var reduced = call(HttpMethod.POST, "/api/v1/beds/wards/" + wardId + "/beds/reduce", adminToken,
        Map.of("count", 6));
    assertThat(reduced.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<Integer>read(reduced.getBody(), "$.data.totalBeds")).isEqualTo(2);

    var beds = call(HttpMethod.GET, "/api/v1/beds/wards/" + wardId + "/beds", adminToken, null);
    assertThat(JsonPath.<List<String>>read(beds.getBody(), "$.data[*].bedNumber"))
        .containsExactly("ICU-01", "ICU-02");
    assertThat(JsonPath.<List<String>>read(beds.getBody(), "$.data[*].status"))
        .containsExactly("OCCUPIED", "MAINTENANCE");
  }

  @Test
  @Order(11)
  void releasingFreesTheBedAndABedThatIsntOccupiedCantBeReleased() {
    var released = call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(0) + "/release", nurseToken, null);
    assertThat(released.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<String>read(released.getBody(), "$.data.status")).isEqualTo("AVAILABLE");

    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(0) + "/release", nurseToken, null)
        .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

    // The patient can now take a different bed.
    var reassigned = call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(0) + "/assign", nurseToken,
        Map.of("patientId", patientB));
    assertThat(reassigned.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @Order(12)
  void addingAfterAReductionNumbersFromTheHighestRemainingBed() {
    var added = call(HttpMethod.POST, "/api/v1/beds/wards/" + wardId + "/beds/add", adminToken,
        Map.of("count", 1));
    assertThat(added.getStatusCode()).isEqualTo(HttpStatus.OK);

    var beds = call(HttpMethod.GET, "/api/v1/beds/wards/" + wardId + "/beds", adminToken, null);
    assertThat(JsonPath.<List<String>>read(beds.getBody(), "$.data[*].bedNumber"))
        .containsExactly("ICU-01", "ICU-02", "ICU-03");
  }

  @Test
  @Order(13)
  void theSummaryCountsUsableCapacityOnly() {
    // ICU-01 occupied, ICU-02 maintenance, ICU-03 available -> 1 of 2 usable beds in use.
    var summary = call(HttpMethod.GET, "/api/v1/beds/summary", adminToken, null);

    assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(JsonPath.<Integer>read(summary.getBody(), "$.data.totalBeds")).isEqualTo(3);
    assertThat(JsonPath.<Integer>read(summary.getBody(), "$.data.occupiedBeds")).isEqualTo(1);
    assertThat(JsonPath.<Integer>read(summary.getBody(), "$.data.maintenanceBeds")).isEqualTo(1);
    assertThat(JsonPath.<Integer>read(summary.getBody(), "$.data.occupancyPercent")).isEqualTo(50);
  }

  @Test
  @Order(14)
  void anotherHospitalCannotSeeOrTouchThisWard() {
    var wards = call(HttpMethod.GET, "/api/v1/beds/wards", otherHospitalToken, null);
    assertThat(JsonPath.<List<?>>read(wards.getBody(), "$.data")).isEmpty();

    assertThat(call(HttpMethod.GET, "/api/v1/beds/wards/" + wardId + "/beds", otherHospitalToken, null)
        .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(call(HttpMethod.POST, "/api/v1/beds/wards/" + wardId + "/beds/reduce",
        otherHospitalToken, Map.of("count", 1)).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(call(HttpMethod.PATCH, "/api/v1/beds/" + bedIds.get(2) + "/release",
        otherHospitalToken, null).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    var summary = call(HttpMethod.GET, "/api/v1/beds/summary", otherHospitalToken, null);
    assertThat(JsonPath.<Integer>read(summary.getBody(), "$.data.totalBeds")).isZero();
  }

  @Test
  @Order(15)
  void anonymousRequestsAreRejected() {
    var response = rest.getForEntity("/api/v1/beds/wards", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  private int wardTotal() {
    var wards = call(HttpMethod.GET, "/api/v1/beds/wards", adminToken, null);
    return JsonPath.read(wards.getBody(), "$.data[0].totalBeds");
  }

  private String register(String email, String hospital) {
    var response = rest.postForEntity("/api/v1/auth/register", json(Map.of(
        "fullName", "Admin User", "email", email, "password", "changeit-123",
        "confirmPassword", "changeit-123", "hospitalName", hospital)), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return JsonPath.read(response.getBody(), "$.data.accessToken");
  }

  private String login(String email) {
    var response = rest.postForEntity("/api/v1/auth/login",
        json(Map.of("email", email, "password", PASSWORD)), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return JsonPath.read(response.getBody(), "$.data.accessToken");
  }

  private Integer createPatient(String firstName, String email) {
    var response = call(HttpMethod.POST, "/api/v1/patients", adminToken, Map.of(
        "firstName", firstName, "lastName", "Patient", "dateOfBirth", "1990-04-12",
        "gender", "FEMALE", "email", email, "bloodGroup", "O+"));
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
