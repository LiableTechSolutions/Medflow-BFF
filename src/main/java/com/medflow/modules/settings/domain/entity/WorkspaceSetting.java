package com.medflow.modules.settings.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A single configuration entry, scoped to one hospital. */
@Entity
@Table(name = "workspace_settings")
public class WorkspaceSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "setting_key", nullable = false, length = 100)
  private String key;

  @Column(name = "setting_value", nullable = false, length = 1000)
  private String value;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected WorkspaceSetting() {
  }

  public WorkspaceSetting(Long hospitalId, String key, String value) {
    this.hospitalId = hospitalId;
    this.key = key;
    this.value = value;
    this.updatedAt = Instant.now();
  }

  public void changeValue(String value) {
    this.value = value;
    this.updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public String getKey() { return key; }
  public String getValue() { return value; }
  public Instant getUpdatedAt() { return updatedAt; }
}
