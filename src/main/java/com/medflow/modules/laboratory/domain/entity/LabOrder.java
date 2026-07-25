package com.medflow.modules.laboratory.domain.entity;

import com.medflow.modules.laboratory.api.LabOrderStatus;
import com.medflow.modules.laboratory.api.LabPriority;
import com.medflow.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lab_orders", indexes = {
    @Index(name = "idx_lab_orders_patient", columnList = "patient_id"),
    @Index(name = "idx_lab_orders_status", columnList = "status")})
public class LabOrder {

  @Id
  private UUID id;

  @Column(name = "patient_id", nullable = false)
  private UUID patientId;

  @Column(name = "ordered_by", nullable = false)
  private UUID orderedBy;

  @Column(nullable = false, length = 150)
  private String testName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LabPriority priority;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LabOrderStatus status;

  @Column(length = 2000)
  private String resultSummary;

  @Column(nullable = false, updatable = false)
  private Instant orderedAt;

  private Instant completedAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected LabOrder() {
  }

  public LabOrder(UUID patientId, UUID orderedBy, String testName, LabPriority priority) {
    this.id = UUID.randomUUID();
    this.patientId = patientId;
    this.orderedBy = orderedBy;
    this.testName = testName;
    this.priority = priority;
    this.status = LabOrderStatus.ORDERED;
    this.orderedAt = Instant.now();
    this.updatedAt = this.orderedAt;
  }

  public void startProcessing() {
    requireStatus(LabOrderStatus.ORDERED, "Only newly ordered tests can start processing");
    this.status = LabOrderStatus.IN_PROGRESS;
    touch();
  }

  public void complete(String resultSummary) {
    if (status != LabOrderStatus.ORDERED && status != LabOrderStatus.IN_PROGRESS) {
      throw new BusinessRuleViolationException(
          "A " + status.name().toLowerCase() + " lab order cannot be completed");
    }
    this.status = LabOrderStatus.COMPLETED;
    this.resultSummary = resultSummary;
    this.completedAt = Instant.now();
    touch();
  }

  public void cancel() {
    if (status == LabOrderStatus.COMPLETED || status == LabOrderStatus.CANCELLED) {
      throw new BusinessRuleViolationException(
          "A " + status.name().toLowerCase() + " lab order cannot be cancelled");
    }
    this.status = LabOrderStatus.CANCELLED;
    touch();
  }

  private void requireStatus(LabOrderStatus expected, String message) {
    if (status != expected) {
      throw new BusinessRuleViolationException(message);
    }
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UUID getPatientId() { return patientId; }
  public UUID getOrderedBy() { return orderedBy; }
  public String getTestName() { return testName; }
  public LabPriority getPriority() { return priority; }
  public LabOrderStatus getStatus() { return status; }
  public String getResultSummary() { return resultSummary; }
  public Instant getOrderedAt() { return orderedAt; }
  public Instant getCompletedAt() { return completedAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
