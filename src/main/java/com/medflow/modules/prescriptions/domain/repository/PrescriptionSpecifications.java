package com.medflow.modules.prescriptions.domain.repository;

import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.modules.prescriptions.domain.entity.Prescription;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Dynamic filters for prescription history; every criterion is optional. */
public final class PrescriptionSpecifications {

  private PrescriptionSpecifications() {
  }

  public static Specification<Prescription> withFilters(UUID patientId, UUID doctorId,
      PrescriptionStatus status) {
    return (root, query, builder) -> {
      var predicates = new ArrayList<Predicate>();
      if (patientId != null) {
        predicates.add(builder.equal(root.get("patientId"), patientId));
      }
      if (doctorId != null) {
        predicates.add(builder.equal(root.get("doctorId"), doctorId));
      }
      if (status != null) {
        predicates.add(builder.equal(root.get("status"), status));
      }
      return builder.and(predicates.toArray(Predicate[]::new));
    };
  }
}
