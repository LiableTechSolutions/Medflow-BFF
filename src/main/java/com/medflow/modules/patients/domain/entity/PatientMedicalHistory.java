package com.medflow.modules.patients.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A condition recorded against a patient, optionally attributed to the recording doctor. */
@Entity
@Table(name = "patient_medical_history")
public class PatientMedicalHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Column(name = "condition_name", nullable = false, length = 200)
  private String conditionName;

  @Column(length = 2000)
  private String notes;

  @Column(name = "recorded_by_doctor_id")
  private Long recordedByDoctorId;

  @Column(name = "recorded_at", nullable = false, updatable = false)
  private Instant recordedAt;

  protected PatientMedicalHistory() {
  }

  public PatientMedicalHistory(Long patientId, String conditionName, String notes,
      Long recordedByDoctorId) {
    this.patientId = patientId;
    this.conditionName = conditionName;
    this.notes = notes;
    this.recordedByDoctorId = recordedByDoctorId;
    this.recordedAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getPatientId() { return patientId; }
  public String getConditionName() { return conditionName; }
  public String getNotes() { return notes; }
  public Long getRecordedByDoctorId() { return recordedByDoctorId; }
  public Instant getRecordedAt() { return recordedAt; }
}
