package com.medflow.modules.appointments.domain.entity;

import com.medflow.modules.appointments.api.AppointmentMode;
import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A scheduled visit. The workflow is enforced here rather than in the service: the
 * aggregate decides which transitions are legal, services only orchestrate.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

  /** Legal next states per current state; anything else is rejected. */
  private static final Map<AppointmentStatus, List<AppointmentStatus>> TRANSITIONS = Map.of(
      AppointmentStatus.BOOKED, List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CHECKED_IN,
          AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW),
      AppointmentStatus.CONFIRMED, List.of(AppointmentStatus.CHECKED_IN,
          AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW),
      AppointmentStatus.CHECKED_IN, List.of(AppointmentStatus.IN_CONSULTATION,
          AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW),
      AppointmentStatus.IN_CONSULTATION, List.of(AppointmentStatus.COMPLETED,
          AppointmentStatus.CANCELLED));

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "doctor_id", nullable = false)
  private Long doctorId;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Column(name = "appointment_mode", nullable = false, length = 20)
  private AppointmentMode appointmentMode;

  @Column(name = "scheduled_at", nullable = false)
  private Instant scheduledAt;

  @Column(name = "duration_minutes", nullable = false)
  private int durationMinutes;

  @Column(nullable = false, length = 30)
  private AppointmentStatus status;

  @Column(name = "queue_number")
  private Integer queueNumber;

  @Column(name = "booked_by_user_id")
  private Long bookedByUserId;

  @Column(length = 255)
  private String reason;

  @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
  private BigDecimal consultationFee;

  @Column(length = 1000)
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Appointment() {
  }

  public Appointment(Long hospitalId, Long doctorId, Long patientId, AppointmentMode mode,
      Instant scheduledAt, int durationMinutes, Integer queueNumber, Long bookedByUserId,
      String reason, BigDecimal consultationFee, String notes) {
    this.hospitalId = hospitalId;
    this.doctorId = doctorId;
    this.patientId = patientId;
    this.appointmentMode = mode;
    this.scheduledAt = scheduledAt;
    this.durationMinutes = durationMinutes;
    this.queueNumber = queueNumber;
    this.bookedByUserId = bookedByUserId;
    this.reason = reason;
    this.consultationFee = consultationFee;
    this.notes = notes;
    this.status = AppointmentStatus.BOOKED;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void transitionTo(AppointmentStatus target) {
    if (!TRANSITIONS.getOrDefault(status, List.of()).contains(target)) {
      throw new BusinessRuleViolationException(
          "Cannot move an appointment from %s to %s".formatted(label(status), label(target)));
    }
    this.status = target;
    touch();
  }

  /** Moving a slot invalidates any prior confirmation, so the status returns to BOOKED. */
  public void reschedule(Instant newScheduledAt) {
    if (status.isClosed()) {
      throw new BusinessRuleViolationException(
          "A %s appointment cannot be rescheduled".formatted(label(status)));
    }
    this.scheduledAt = newScheduledAt;
    this.status = AppointmentStatus.BOOKED;
    touch();
  }

  public Instant endsAt() {
    return scheduledAt.plusSeconds(durationMinutes * 60L);
  }

  public boolean overlaps(Instant otherStart, Instant otherEnd) {
    return status != AppointmentStatus.CANCELLED
        && scheduledAt.isBefore(otherEnd)
        && otherStart.isBefore(endsAt());
  }

  private String label(AppointmentStatus status) {
    return status.name().toLowerCase().replace('_', ' ');
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public Long getDoctorId() { return doctorId; }
  public Long getPatientId() { return patientId; }
  public AppointmentMode getAppointmentMode() { return appointmentMode; }
  public Instant getScheduledAt() { return scheduledAt; }
  public int getDurationMinutes() { return durationMinutes; }
  public AppointmentStatus getStatus() { return status; }
  public Integer getQueueNumber() { return queueNumber; }
  public Long getBookedByUserId() { return bookedByUserId; }
  public String getReason() { return reason; }
  public BigDecimal getConsultationFee() { return consultationFee; }
  public String getNotes() { return notes; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
