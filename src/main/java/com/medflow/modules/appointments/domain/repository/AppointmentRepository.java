package com.medflow.modules.appointments.domain.repository;

import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.modules.appointments.domain.entity.Appointment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository
    extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

  Optional<Appointment> findByIdAndHospitalId(Long id, Long hospitalId);

  List<Appointment> findByDoctorIdAndScheduledAtBetween(Long doctorId, Instant from, Instant to);

  long countByHospitalIdAndScheduledAtBetweenAndStatusNot(Long hospitalId, Instant from,
      Instant to, AppointmentStatus excluded);

  long countByHospitalIdAndStatusIn(Long hospitalId, Collection<AppointmentStatus> statuses);

  @Query("""
      SELECT COALESCE(MAX(a.queueNumber), 0) FROM Appointment a
      WHERE a.doctorId = :doctorId AND a.scheduledAt >= :from AND a.scheduledAt < :to
      """)
  int highestQueueNumber(@Param("doctorId") Long doctorId, @Param("from") Instant from,
      @Param("to") Instant to);

  @Query("""
      SELECT COALESCE(SUM(a.consultationFee), 0)
      FROM Appointment a
      WHERE a.hospitalId = :hospitalId
        AND a.status = com.medflow.modules.appointments.api.AppointmentStatus.COMPLETED
        AND a.scheduledAt >= :from AND a.scheduledAt < :to
      """)
  BigDecimal sumCompletedFeesBetween(@Param("hospitalId") Long hospitalId,
      @Param("from") Instant from, @Param("to") Instant to);

  @Query(nativeQuery = true, value = """
      SELECT CAST(scheduled_at AS DATE) AS visit_day, COUNT(*) AS total
      FROM appointments
      WHERE hospital_id = :hospitalId
        AND scheduled_at >= :from AND scheduled_at < :to
        AND status <> 'cancelled'
      GROUP BY CAST(scheduled_at AS DATE)
      ORDER BY visit_day
      """)
  List<DailyCountProjection> countDailyBetween(@Param("hospitalId") Long hospitalId,
      @Param("from") Instant from, @Param("to") Instant to);

  /** "day" is a reserved word in some engines, so the column is aliased visit_day. */
  interface DailyCountProjection {
    LocalDate getVisitDay();

    long getTotal();
  }
}
