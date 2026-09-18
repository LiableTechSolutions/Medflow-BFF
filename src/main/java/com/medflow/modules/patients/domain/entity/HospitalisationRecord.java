package com.medflow.modules.patients.domain.entity;

import com.medflow.modules.patients.api.HospitalisationStatus;
import com.medflow.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One inpatient stay. A patient can have several of these over time (readmissions); the
 * most recent one determines whether {@code patients.is_hospitalised} is set. Discharging
 * never deletes the row, so the stay (and its daily analyses) remains in the patient's history.
 */
@Entity
@Table(name = "hospitalisation_records")
public class HospitalisationRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Column(name = "admission_date", nullable = false)
  private Instant admissionDate;

  @Column(name = "discharge_date")
  private Instant dischargeDate;

  @Column(length = 50)
  private String ward;

  @Column(length = 20)
  private String bed;

  @Column(name = "admitting_doctor_id", nullable = false)
  private Long admittingDoctorId;

  @Column(nullable = false, length = 20)
  private HospitalisationStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected HospitalisationRecord() {
  }

  public HospitalisationRecord(Long hospitalId, Long patientId, Instant admissionDate,
      String ward, String bed, Long admittingDoctorId) {
    this.hospitalId = hospitalId;
    this.patientId = patientId;
    this.admissionDate = admissionDate != null ? admissionDate : Instant.now();
    this.ward = ward;
    this.bed = bed;
    this.admittingDoctorId = admittingDoctorId;
    this.status = HospitalisationStatus.ADMITTED;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void discharge(Instant dischargeDate) {
    if (status != HospitalisationStatus.ADMITTED) {
      throw new BusinessRuleViolationException("Only an admitted stay can be discharged");
    }
    this.status = HospitalisationStatus.DISCHARGED;
    this.dischargeDate = dischargeDate != null ? dischargeDate : Instant.now();
    touch();
  }

  public boolean isActive() {
    return status == HospitalisationStatus.ADMITTED;
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public Long getPatientId() { return patientId; }
  public Instant getAdmissionDate() { return admissionDate; }
  public Instant getDischargeDate() { return dischargeDate; }
  public String getWard() { return ward; }
  public String getBed() { return bed; }
  public Long getAdmittingDoctorId() { return admittingDoctorId; }
  public HospitalisationStatus getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
