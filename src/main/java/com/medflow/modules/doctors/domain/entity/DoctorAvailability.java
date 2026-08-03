package com.medflow.modules.doctors.domain.entity;

import com.medflow.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * One consulting window for a doctor: either a recurring weekday or a one-off date.
 * The invariant — exactly one of the two — is enforced here and by a CHECK constraint.
 */
@Entity
@Table(name = "doctor_availability")
public class DoctorAvailability {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "doctor_id", nullable = false)
  private Long doctorId;

  @Column(name = "day_of_week")
  private Integer dayOfWeek;

  @Column(name = "specific_date")
  private LocalDate specificDate;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "slot_duration_minutes", nullable = false)
  private int slotDurationMinutes;

  @Column(name = "is_available", nullable = false)
  private boolean available;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected DoctorAvailability() {
  }

  public DoctorAvailability(Long doctorId, Integer dayOfWeek, LocalDate specificDate,
      LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes, Boolean available) {
    if (dayOfWeek == null && specificDate == null) {
      throw new BusinessRuleViolationException(
          "Availability needs either a day of week or a specific date");
    }
    if (!startTime.isBefore(endTime)) {
      throw new BusinessRuleViolationException("Start time must be before end time");
    }
    this.doctorId = doctorId;
    this.dayOfWeek = dayOfWeek;
    this.specificDate = specificDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.slotDurationMinutes = slotDurationMinutes == null ? 30 : slotDurationMinutes;
    this.available = available == null || available;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getDoctorId() { return doctorId; }
  public Integer getDayOfWeek() { return dayOfWeek; }
  public LocalDate getSpecificDate() { return specificDate; }
  public LocalTime getStartTime() { return startTime; }
  public LocalTime getEndTime() { return endTime; }
  public int getSlotDurationMinutes() { return slotDurationMinutes; }
  public boolean isAvailable() { return available; }
  public Instant getCreatedAt() { return createdAt; }
}
