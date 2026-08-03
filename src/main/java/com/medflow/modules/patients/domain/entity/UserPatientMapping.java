package com.medflow.modules.patients.domain.entity;

import com.medflow.modules.patients.api.MappingRelation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Links a portal account to a patient record. One account can act for several patients
 * (a parent booking for their children) and one patient can be reachable through several
 * accounts (both parents, a caretaker).
 */
@Entity
@Table(name = "user_patient_mapping")
public class UserPatientMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Column(nullable = false, length = 20)
  private MappingRelation relation;

  @Column(name = "is_primary_contact", nullable = false)
  private boolean primaryContact;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected UserPatientMapping() {
  }

  public UserPatientMapping(Long userId, Long patientId, MappingRelation relation,
      boolean primaryContact) {
    this.userId = userId;
    this.patientId = patientId;
    this.relation = relation;
    this.primaryContact = primaryContact;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public Long getPatientId() { return patientId; }
  public MappingRelation getRelation() { return relation; }
  public boolean isPrimaryContact() { return primaryContact; }
  public Instant getCreatedAt() { return createdAt; }
}
