package com.medflow.modules.tenancy.domain.entity;

import com.medflow.modules.tenancy.api.EntitlementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Grants one hospital access to one module, optionally until an expiry date. */
@Entity
@Table(name = "hospital_module_entitlements")
public class ModuleEntitlement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "module_id", nullable = false)
  private Integer moduleId;

  @Column(nullable = false, length = 30)
  private EntitlementStatus status;

  @Column(name = "activated_at")
  private Instant activatedAt;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected ModuleEntitlement() {
  }

  public ModuleEntitlement(Long hospitalId, Integer moduleId, EntitlementStatus status) {
    this.hospitalId = hospitalId;
    this.moduleId = moduleId;
    this.status = status;
    this.createdAt = Instant.now();
    this.activatedAt = status.grantsAccess() ? this.createdAt : null;
  }

  /** True when the grant is live: an access-granting status that has not lapsed. */
  public boolean isAccessible(Instant now) {
    return status.grantsAccess() && (expiresAt == null || now.isBefore(expiresAt));
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public Integer getModuleId() { return moduleId; }
  public EntitlementStatus getStatus() { return status; }
  public Instant getActivatedAt() { return activatedAt; }
  public Instant getExpiresAt() { return expiresAt; }
  public Instant getCreatedAt() { return createdAt; }
}
