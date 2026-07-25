package com.medflow.modules.appointments.domain.repository;

import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.modules.appointments.domain.entity.Appointment;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Dynamic filters for the appointment list; every criterion is optional. */
public final class AppointmentSpecifications {

  private AppointmentSpecifications() {
  }

  public static Specification<Appointment> withFilters(AppointmentStatus status, UUID doctorId,
      UUID patientId, LocalDate date) {
    return (root, query, builder) -> {
      var predicates = new ArrayList<Predicate>();
      if (status != null) {
        predicates.add(builder.equal(root.get("status"), status));
      }
      if (doctorId != null) {
        predicates.add(builder.equal(root.get("doctorId"), doctorId));
      }
      if (patientId != null) {
        predicates.add(builder.equal(root.get("patientId"), patientId));
      }
      if (date != null) {
        var start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        var end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        predicates.add(builder.greaterThanOrEqualTo(root.get("scheduledAt"), start));
        predicates.add(builder.lessThan(root.get("scheduledAt"), end));
      }
      return builder.and(predicates.toArray(Predicate[]::new));
    };
  }
}
