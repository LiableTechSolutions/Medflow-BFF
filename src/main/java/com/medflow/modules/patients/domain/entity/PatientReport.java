package com.medflow.modules.patients.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Pointer to a document stored outside the database (scan, lab report, discharge note). */
@Entity
@Table(name = "patient_reports")
public class PatientReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "report_type", nullable = false, length = 100)
  private String reportType;

  @Column(name = "file_url", nullable = false, length = 255)
  private String fileUrl;

  @Column(name = "uploaded_by_user_id")
  private Long uploadedByUserId;

  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private Instant uploadedAt;

  protected PatientReport() {
  }

  public PatientReport(Long patientId, Long hospitalId, String reportType, String fileUrl,
      Long uploadedByUserId) {
    this.patientId = patientId;
    this.hospitalId = hospitalId;
    this.reportType = reportType;
    this.fileUrl = fileUrl;
    this.uploadedByUserId = uploadedByUserId;
    this.uploadedAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getPatientId() { return patientId; }
  public Long getHospitalId() { return hospitalId; }
  public String getReportType() { return reportType; }
  public String getFileUrl() { return fileUrl; }
  public Long getUploadedByUserId() { return uploadedByUserId; }
  public Instant getUploadedAt() { return uploadedAt; }
}
