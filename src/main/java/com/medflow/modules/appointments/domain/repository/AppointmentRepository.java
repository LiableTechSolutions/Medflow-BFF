package com.medflow.modules.appointments.domain.repository;

import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.modules.appointments.domain.entity.Appointment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository
    extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {

  List<Appointment> findByDoctorIdAndScheduledAtBetween(UUID doctorId, Instant from, Instant to);

  long countByScheduledAtBetweenAndStatusNot(Instant from, Instant to, AppointmentStatus excluded);

  long countByStatusIn(Collection<AppointmentStatus> statuses);

  @Query("""
      SELECT COALESCE(SUM(a.consultationFee), 0)
      FROM Appointment a
      WHERE a.status = com.medflow.modules.appointments.api.AppointmentStatus.COMPLETED
        AND a.scheduledAt >= :from AND a.scheduledAt < :to
      """)
  BigDecimal sumCompletedFeesBetween(@Param("from") Instant from, @Param("to") Instant to);

  @Query(nativeQuery = true, value = """
      SELECT CAST(scheduled_at AS DATE) AS day, COUNT(*) AS total
      FROM appointments
      WHERE scheduled_at >= :from AND scheduled_at < :to AND status <> 'CANCELLED'
      GROUP BY CAST(scheduled_at AS DATE)
      ORDER BY day
      """)
  List<DailyCountProjection> countDailyBetween(@Param("from") Instant from, @Param("to") Instant to);

  interface DailyCountProjection {
    LocalDate getDay();

    long getTotal();
  }
}
