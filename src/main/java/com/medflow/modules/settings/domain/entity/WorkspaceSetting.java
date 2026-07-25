package com.medflow.modules.settings.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "workspace_settings")
public class WorkspaceSetting {

  @Id
  @Column(name = "setting_key", length = 100)
  private String key;

  @Column(name = "setting_value", nullable = false, length = 1000)
  private String value;

  @Column(nullable = false)
  private Instant updatedAt;

  protected WorkspaceSetting() {
  }

  public WorkspaceSetting(String key, String value) {
    this.key = key;
    this.value = value;
    this.updatedAt = Instant.now();
  }

  public void changeValue(String value) {
    this.value = value;
    this.updatedAt = Instant.now();
  }

  public String getKey() { return key; }
  public String getValue() { return value; }
  public Instant getUpdatedAt() { return updatedAt; }
}
