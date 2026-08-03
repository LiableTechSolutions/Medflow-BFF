package com.medflow.modules.rbac.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Which product module a portal group may open. */
@Entity
@Table(name = "user_group_portal_access")
public class UserGroupPortalAccess {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_group_id", nullable = false)
  private Integer userGroupId;

  @Column(name = "module_id", nullable = false)
  private Integer moduleId;

  @Column(name = "can_access", nullable = false)
  private boolean canAccess;

  protected UserGroupPortalAccess() {
  }

  public Long getId() { return id; }
  public Integer getUserGroupId() { return userGroupId; }
  public Integer getModuleId() { return moduleId; }
  public boolean isCanAccess() { return canAccess; }
}
