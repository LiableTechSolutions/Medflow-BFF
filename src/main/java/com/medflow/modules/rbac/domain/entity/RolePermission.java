package com.medflow.modules.rbac.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Grants one permission to one role. A null {@code hospitalId} is the platform-wide
 * default; a row carrying a hospital id adds a grant for that tenant only.
 */
@Entity
@Table(name = "role_permissions")
public class RolePermission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "role_id", nullable = false)
  private Integer roleId;

  @Column(name = "permission_id", nullable = false)
  private Integer permissionId;

  @Column(name = "hospital_id")
  private Long hospitalId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected RolePermission() {
  }

  public RolePermission(Integer roleId, Integer permissionId, Long hospitalId) {
    this.roleId = roleId;
    this.permissionId = permissionId;
    this.hospitalId = hospitalId;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public Integer getRoleId() { return roleId; }
  public Integer getPermissionId() { return permissionId; }
  public Long getHospitalId() { return hospitalId; }
  public Instant getCreatedAt() { return createdAt; }
}
