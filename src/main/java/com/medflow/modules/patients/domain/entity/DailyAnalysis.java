package com.medflow.modules.patients.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A doctor's vitals/notes entry recorded against an active {@link HospitalisationRecord},
 * one per round. Ordered by {@code recordedAt}, these form the trend a doctor reviews.
 */
@Entity
@Table(name = "daily_analyses")
public class DailyAnalysis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospitalisation_record_id", nullable = false)
  private Long hospitalisationRecordId;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Column(name = "blood_pressure", length = 20)
  private String bloodPressure;

  private Integer pulse;

  @Column(precision = 4, scale = 1)
  private BigDecimal temperature;

  private Integer spo2;

  @Column(length = 2000)
  private String notes;

  @Column(name = "recorded_by_doctor_id", nullable = false)
  private Long recordedByDoctorId;

  @Column(name = "recorded_at", nullable = false, updatable = false)
  private Instant recordedAt;

  protected DailyAnalysis() {
  }

  public DailyAnalysis(Long hospitalisationRecordId, Long patientId, String bloodPressure,
      Integer pulse, BigDecimal temperature, Integer spo2, String notes,
      Long recordedByDoctorId) {
    this.hospitalisationRecordId = hospitalisationRecordId;
    this.patientId = patientId;
    this.bloodPressure = bloodPressure;
    this.pulse = pulse;
    this.temperature = temperature;
    this.spo2 = spo2;
    this.notes = notes;
    this.recordedByDoctorId = recordedByDoctorId;
    this.recordedAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getHospitalisationRecordId() { return hospitalisationRecordId; }
  public Long getPatientId() { return patientId; }
  public String getBloodPressure() { return bloodPressure; }
  public Integer getPulse() { return pulse; }
  public BigDecimal getTemperature() { return temperature; }
  public Integer getSpo2() { return spo2; }
  public String getNotes() { return notes; }
  public Long getRecordedByDoctorId() { return recordedByDoctorId; }
  public Instant getRecordedAt() { return recordedAt; }
}
