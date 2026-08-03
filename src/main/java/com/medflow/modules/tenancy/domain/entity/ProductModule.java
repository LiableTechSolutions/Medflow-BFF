package com.medflow.modules.tenancy.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A licensable product module ({@code modules_master}). Rows are reference data shipped
 * with the schema — one per entry in the UI's navigation, plus roadmap modules whose
 * phase is greater than 1.
 */
@Entity
@Table(name = "modules_master")
public class ProductModule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "module_code", nullable = false, length = 50)
  private String moduleCode;

  @Column(name = "module_name", nullable = false, length = 100)
  private String moduleName;

  @Column(nullable = false)
  private int phase;

  @Column(length = 500)
  private String description;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  protected ProductModule() {
  }

  public Integer getId() { return id; }
  public String getModuleCode() { return moduleCode; }
  public String getModuleName() { return moduleName; }
  public int getPhase() { return phase; }
  public String getDescription() { return description; }
  public boolean isActive() { return active; }
}
