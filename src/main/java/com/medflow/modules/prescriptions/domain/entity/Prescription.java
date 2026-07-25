package com.medflow.modules.prescriptions.domain.entity;

import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prescriptions", indexes = {
    @Index(name = "idx_prescriptions_patient", columnList = "patient_id"),
    @Index(name = "idx_prescriptions_doctor", columnList = "doctor_id")})
public class Prescription {

  @Id
  private UUID id;

  @Column(name = "patient_id", nullable = false)
  private UUID patientId;

  @Column(name = "doctor_id", nullable = false)
  private UUID doctorId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PrescriptionStatus status;

  @Column(length = 1000)
  private String notes;

  @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("medicationName ASC")
  private List<PrescriptionItem> items = new ArrayList<>();

  @Column(nullable = false, updatable = false)
  private Instant issuedAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected Prescription() {
  }

  public Prescription(UUID patientId, UUID doctorId, String notes) {
    this.id = UUID.randomUUID();
    this.patientId = patientId;
    this.doctorId = doctorId;
    this.notes = notes;
    this.status = PrescriptionStatus.ACTIVE;
    this.issuedAt = Instant.now();
    this.updatedAt = this.issuedAt;
  }

  public void addItem(String medicationName, String dosage, String frequency,
      Integer durationDays, String instructions) {
    items.add(new PrescriptionItem(this, medicationName, dosage, frequency, durationDays,
        instructions));
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

  public UUID getId() { return id; }
  public UUID getPatientId() { return patientId; }
  public UUID getDoctorId() { return doctorId; }
  public PrescriptionStatus getStatus() { return status; }
  public String getNotes() { return notes; }
  public List<PrescriptionItem> getItems() { return List.copyOf(items); }
  public Instant getIssuedAt() { return issuedAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
