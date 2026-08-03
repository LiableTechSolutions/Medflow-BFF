package com.medflow.modules.rbac.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A portal: the shell an account lands in after signing in. */
@Entity
@Table(name = "user_groups")
public class UserGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "group_code", nullable = false, length = 50)
  private String groupCode;

  @Column(name = "group_name", nullable = false, length = 100)
  private String groupName;

  @Column(length = 500)
  private String description;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  protected UserGroup() {
  }

  public Integer getId() { return id; }
  public String getGroupCode() { return groupCode; }
  public String getGroupName() { return groupName; }
  public String getDescription() { return description; }
  public boolean isActive() { return active; }
}
