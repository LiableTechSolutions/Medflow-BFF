package com.medflow.modules.appointments.domain.entity;

import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appointments_doctor_time", columnList = "doctor_id, scheduled_at"),
    @Index(name = "idx_appointments_patient", columnList = "patient_id"),
    @Index(name = "idx_appointments_status", columnList = "status")})
public class Appointment {

  @Id
  private UUID id;

  @Column(name = "patient_id", nullable = false)
  private UUID patientId;

  @Column(name = "doctor_id", nullable = false)
  private UUID doctorId;

  @Column(name = "scheduled_at", nullable = false)
  private Instant scheduledAt;

  @Column(nullable = false)
  private int durationMinutes;

  @Column(length = 255)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AppointmentStatus status;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal consultationFee;

  @Column(length = 1000)
  private String notes;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected Appointment() {
  }

  public Appointment(UUID patientId, UUID doctorId, Instant scheduledAt, int durationMinutes,
      String reason, BigDecimal consultationFee, String notes) {
    this.id = UUID.randomUUID();
    this.patientId = patientId;
    this.doctorId = doctorId;
    this.scheduledAt = scheduledAt;
    this.durationMinutes = durationMinutes;
    this.reason = reason;
    this.consultationFee = consultationFee;
    this.notes = notes;
    this.status = AppointmentStatus.PENDING;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void confirm() {
    requireStatus(AppointmentStatus.PENDING, "Only pending appointments can be confirmed");
    this.status = AppointmentStatus.CONFIRMED;
    touch();
  }

  public void complete() {
    requireStatus(AppointmentStatus.CONFIRMED, "Only confirmed appointments can be completed");
    this.status = AppointmentStatus.COMPLETED;
    touch();
  }

  public void cancel() {
    if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELLED) {
      throw new BusinessRuleViolationException(
          "A " + status.name().toLowerCase() + " appointment cannot be cancelled");
    }
    this.status = AppointmentStatus.CANCELLED;
    touch();
  }

  /** Moving a slot invalidates any prior confirmation, so the status returns to PENDING. */
  public void reschedule(Instant newScheduledAt) {
    if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELLED) {
      throw new BusinessRuleViolationException(
          "A " + status.name().toLowerCase() + " appointment cannot be rescheduled");
    }
    this.scheduledAt = newScheduledAt;
    this.status = AppointmentStatus.PENDING;
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

  private void requireStatus(AppointmentStatus expected, String message) {
    if (status != expected) {
      throw new BusinessRuleViolationException(message);
    }
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UUID getPatientId() { return patientId; }
  public UUID getDoctorId() { return doctorId; }
  public Instant getScheduledAt() { return scheduledAt; }
  public int getDurationMinutes() { return durationMinutes; }
  public String getReason() { return reason; }
  public AppointmentStatus getStatus() { return status; }
  public BigDecimal getConsultationFee() { return consultationFee; }
  public String getNotes() { return notes; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
