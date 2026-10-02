package com.medflow.modules.prescriptions.domain.repository;

import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.modules.prescriptions.domain.entity.Prescription;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Dynamic filters for prescription history; the tenant predicate is never optional. */
public final class PrescriptionSpecifications {

  private PrescriptionSpecifications() {
  }

  public static Specification<Prescription> withFilters(Long hospitalId, Long patientId,
      Long doctorId, PrescriptionStatus status, LocalDate issuedOn, String diagnosisQuery,
      List<Long> matchingPatientIds, List<Long> matchingDoctorIds) {
    return (root, query, builder) -> {
      var predicates = new ArrayList<Predicate>();
      predicates.add(builder.equal(root.get("hospitalId"), hospitalId));
      if (patientId != null) {
        predicates.add(builder.equal(root.get("patientId"), patientId));
      }
      if (doctorId != null) {
        predicates.add(builder.equal(root.get("doctorId"), doctorId));
      }
      if (status != null) {
        predicates.add(builder.equal(root.get("status"), status));
      }
      if (issuedOn != null) {
        var start = issuedOn.atStartOfDay(ZoneOffset.UTC).toInstant();
        var end = issuedOn.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), start));
        predicates.add(builder.lessThan(root.get("createdAt"), end));
      }
      // Free-text search: diagnosis text, or the patient/doctor name resolved to ids by
      // the service layer (this specification never calls another module directly).
      if (diagnosisQuery != null) {
        var textMatches = new ArrayList<Predicate>();
        textMatches.add(
            builder.like(builder.lower(root.get("diagnosis")), "%" + diagnosisQuery.toLowerCase() + "%"));
        if (!matchingPatientIds.isEmpty()) {
          textMatches.add(root.get("patientId").in(matchingPatientIds));
        }
        if (!matchingDoctorIds.isEmpty()) {
          textMatches.add(root.get("doctorId").in(matchingDoctorIds));
        }
        predicates.add(builder.or(textMatches.toArray(Predicate[]::new)));
      }
      return builder.and(predicates.toArray(Predicate[]::new));
    };
  }
}
