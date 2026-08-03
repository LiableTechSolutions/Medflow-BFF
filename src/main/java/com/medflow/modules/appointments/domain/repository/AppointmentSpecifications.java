package com.medflow.modules.appointments.domain.repository;

import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.modules.appointments.domain.entity.Appointment;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

/** Dynamic filters for the appointment list; the tenant predicate is never optional. */
public final class AppointmentSpecifications {

  private AppointmentSpecifications() {
  }

  public static Specification<Appointment> withFilters(Long hospitalId, AppointmentStatus status,
      Long doctorId, Long patientId, LocalDate date) {
    return (root, query, builder) -> {
      var predicates = new ArrayList<Predicate>();
      predicates.add(builder.equal(root.get("hospitalId"), hospitalId));
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
