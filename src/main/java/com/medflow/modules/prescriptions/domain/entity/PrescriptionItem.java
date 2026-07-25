package com.medflow.modules.prescriptions.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "prescription_items")
public class PrescriptionItem {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "prescription_id", nullable = false)
  private Prescription prescription;

  @Column(nullable = false, length = 150)
  private String medicationName;

  @Column(nullable = false, length = 50)
  private String dosage;

  @Column(nullable = false, length = 50)
  private String frequency;

  private Integer durationDays;

  @Column(length = 255)
  private String instructions;

  protected PrescriptionItem() {
  }

  PrescriptionItem(Prescription prescription, String medicationName, String dosage,
      String frequency, Integer durationDays, String instructions) {
    this.id = UUID.randomUUID();
    this.prescription = prescription;
    this.medicationName = medicationName;
    this.dosage = dosage;
    this.frequency = frequency;
    this.durationDays = durationDays;
    this.instructions = instructions;
  }

  public UUID getId() { return id; }
  public Prescription getPrescription() { return prescription; }
  public String getMedicationName() { return medicationName; }
  public String getDosage() { return dosage; }
  public String getFrequency() { return frequency; }
  public Integer getDurationDays() { return durationDays; }
  public String getInstructions() { return instructions; }
}
