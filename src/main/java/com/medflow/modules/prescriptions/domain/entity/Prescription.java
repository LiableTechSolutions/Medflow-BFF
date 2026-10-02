package com.medflow.modules.prescriptions.domain.entity;

import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Entity
@Table(name = "prescriptions")
public class Prescription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "appointment_id")
  private Long appointmentId;

  @Column(name = "doctor_id", nullable = false)
  private Long doctorId;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Column(length = 2000)
  private String diagnosis;

  /** Serialized medication lines; see {@code PrescriptionItemResponse} for the shape. */
  @Column(name = "medicines_json", nullable = false, length = 8000)
  private String medicinesJson;

  @Column(name = "digitally_signed", nullable = false)
  private boolean digitallySigned;

  /** When the patient should come back; a daily job reminds them the day before. */
  @Column(name = "follow_up_date")
  private LocalDate followUpDate;

  @Column(name = "signed_at")
  private Instant signedAt;

  @Column(nullable = false, length = 20)
  private PrescriptionStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Prescription() {
  }

  public Prescription(Long hospitalId, Long appointmentId, Long doctorId, Long patientId,
      String diagnosis, String medicinesJson, boolean digitallySigned, LocalDate followUpDate) {
    this.hospitalId = hospitalId;
    this.appointmentId = appointmentId;
    this.doctorId = doctorId;
    this.patientId = patientId;
    this.diagnosis = diagnosis;
    this.medicinesJson = medicinesJson;
    this.digitallySigned = digitallySigned;
    this.followUpDate = followUpDate;
    this.status = PrescriptionStatus.ACTIVE;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
    this.signedAt = digitallySigned ? this.createdAt : null;
  }

  /** Still open for edits: active, and only until the day it was issued is over (UTC). */
  public boolean isEditable() {
    return status == PrescriptionStatus.ACTIVE
        && createdAt.atZone(ZoneOffset.UTC).toLocalDate().equals(LocalDate.now(ZoneOffset.UTC));
  }

  public void update(String diagnosis, String medicinesJson, boolean digitallySigned,
      LocalDate followUpDate) {
    requireActive("Only active prescriptions can be edited");
    if (!isEditable()) {
      throw new BusinessRuleViolationException(
          "A prescription can only be edited on the day it was issued");
    }
    this.diagnosis = diagnosis;
    this.medicinesJson = medicinesJson;
    this.followUpDate = followUpDate;
    if (digitallySigned && this.signedAt == null) {
      this.signedAt = Instant.now();
    } else if (!digitallySigned) {
      this.signedAt = null;
    }
    this.digitallySigned = digitallySigned;
    touch();
  }

  public void complete() {
    requireActive("Only active prescriptions can be completed");
    this.status = PrescriptionStatus.COMPLETED;
    touch();
  }

  public void cancel() {
    requireActive("Only active prescriptions can be cancelled");
    this.status = PrescriptionStatus.CANCELLED;
    touch();
  }

  private void requireActive(String message) {
    if (status != PrescriptionStatus.ACTIVE) {
      throw new BusinessRuleViolationException(message);
    }
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public Long getAppointmentId() { return appointmentId; }
  public Long getDoctorId() { return doctorId; }
  public Long getPatientId() { return patientId; }
  public String getDiagnosis() { return diagnosis; }
  public String getMedicinesJson() { return medicinesJson; }
  public boolean isDigitallySigned() { return digitallySigned; }
  public LocalDate getFollowUpDate() { return followUpDate; }
  public Instant getSignedAt() { return signedAt; }
  public PrescriptionStatus getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
