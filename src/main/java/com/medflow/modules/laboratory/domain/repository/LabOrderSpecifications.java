package com.medflow.modules.laboratory.domain.repository;

import com.medflow.modules.laboratory.api.LabOrderStatus;
import com.medflow.modules.laboratory.api.LabPriority;
import com.medflow.modules.laboratory.domain.entity.LabOrder;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Dynamic filters for the specimen queue; every criterion is optional. */
public final class LabOrderSpecifications {

  private LabOrderSpecifications() {
  }

  public static Specification<LabOrder> withFilters(LabOrderStatus status, LabPriority priority,
      UUID patientId) {
    return (root, query, builder) -> {
      var predicates = new ArrayList<Predicate>();
      if (status != null) {
        predicates.add(builder.equal(root.get("status"), status));
      }
      if (priority != null) {
        predicates.add(builder.equal(root.get("priority"), priority));
      }
      if (patientId != null) {
        predicates.add(builder.equal(root.get("patientId"), patientId));
      }
      return builder.and(predicates.toArray(Predicate[]::new));
    };
  }
}
