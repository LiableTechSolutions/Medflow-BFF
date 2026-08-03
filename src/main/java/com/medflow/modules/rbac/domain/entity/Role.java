package com.medflow.modules.rbac.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "roles")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "role_code", nullable = false, length = 50)
  private String roleCode;

  @Column(name = "role_name", nullable = false, length = 100)
  private String roleName;

  @Column(length = 500)
  private String description;

  @Column(name = "is_system_role", nullable = false)
  private boolean systemRole;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Role() {
  }

  public Integer getId() { return id; }
  public String getRoleCode() { return roleCode; }
  public String getRoleName() { return roleName; }
  public String getDescription() { return description; }
  public boolean isSystemRole() { return systemRole; }
  public Instant getCreatedAt() { return createdAt; }
}
