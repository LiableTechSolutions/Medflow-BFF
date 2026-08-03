package com.medflow.modules.doctors.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Attaches a staff account to a doctor — the front-desk agent who manages their diary,
 * the nurse who preps their patients. Drives "my doctors" views for support staff.
 */
@Entity
@Table(name = "user_doctor_mapping")
public class UserDoctorMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "doctor_id", nullable = false)
  private Long doctorId;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "relation_type", nullable = false, length = 50)
  private String relationType;

  @Column(name = "is_primary", nullable = false)
  private boolean primary;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected UserDoctorMapping() {
  }

  public UserDoctorMapping(Long userId, Long doctorId, Long hospitalId, String relationType,
      boolean primary) {
    this.userId = userId;
    this.doctorId = doctorId;
    this.hospitalId = hospitalId;
    this.relationType = relationType;
    this.primary = primary;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public Long getDoctorId() { return doctorId; }
  public Long getHospitalId() { return hospitalId; }
  public String getRelationType() { return relationType; }
  public boolean isPrimary() { return primary; }
  public Instant getCreatedAt() { return createdAt; }
}
