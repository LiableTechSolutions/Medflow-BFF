package com.medflow.modules.rbac.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions")
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "permission_code", nullable = false, length = 100)
  private String permissionCode;

  @Column(name = "module_id")
  private Integer moduleId;

  @Column(name = "permission_name", nullable = false, length = 150)
  private String permissionName;

  @Column(length = 500)
  private String description;

  protected Permission() {
  }

  public Integer getId() { return id; }
  public String getPermissionCode() { return permissionCode; }
  public Integer getModuleId() { return moduleId; }
  public String getPermissionName() { return permissionName; }
  public String getDescription() { return description; }
}
