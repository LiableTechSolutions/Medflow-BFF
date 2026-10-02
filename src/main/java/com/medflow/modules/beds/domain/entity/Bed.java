package com.medflow.modules.beds.domain.entity;

import com.medflow.modules.beds.api.BedStatus;
import com.medflow.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "beds")
public class Bed {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "ward_id", nullable = false)
  private Long wardId;

  @Column(name = "bed_number", nullable = false)
  private int bedNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BedStatus status;

  @Column(name = "patient_id")
  private Long patientId;

  @Column(name = "occupied_at")
  private Instant occupiedAt;

  protected Bed() {
  }

  public Bed(Long hospitalId, Long wardId, int bedNumber) {
    this.hospitalId = hospitalId;
    this.wardId = wardId;
    this.bedNumber = bedNumber;
    this.status = BedStatus.AVAILABLE;
  }

  public void assign(Long patientId) {
    if (status == BedStatus.OCCUPIED) {
      throw new BusinessRuleViolationException("This bed is already occupied");
    }
    if (status == BedStatus.MAINTENANCE) {
      throw new BusinessRuleViolationException("This bed is under maintenance");
    }
    this.status = BedStatus.OCCUPIED;
    this.patientId = patientId;
    this.occupiedAt = Instant.now();
  }

  public void release() {
    if (status != BedStatus.OCCUPIED) {
      throw new BusinessRuleViolationException("This bed is not occupied");
    }
    this.status = BedStatus.AVAILABLE;
    this.patientId = null;
    this.occupiedAt = null;
  }

  public void setMaintenance(boolean underMaintenance) {
    if (status == BedStatus.OCCUPIED) {
      throw new BusinessRuleViolationException(
          "An occupied bed can't go into maintenance — release it first");
    }
    this.status = underMaintenance ? BedStatus.MAINTENANCE : BedStatus.AVAILABLE;
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public Long getWardId() { return wardId; }
  public int getBedNumber() { return bedNumber; }
  public BedStatus getStatus() { return status; }
  public Long getPatientId() { return patientId; }
  public Instant getOccupiedAt() { return occupiedAt; }
}
