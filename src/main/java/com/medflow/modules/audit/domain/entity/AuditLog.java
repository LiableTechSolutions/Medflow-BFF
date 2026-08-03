package com.medflow.modules.audit.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** One immutable trail entry. There are deliberately no mutators. */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "user_id")
  private Long userId;

  @Column(nullable = false, length = 100)
  private String action;

  @Column(name = "entity_type", nullable = false, length = 100)
  private String entityType;

  @Column(name = "entity_id")
  private Long entityId;

  @Column(name = "metadata_json", length = 4000)
  private String metadataJson;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected AuditLog() {
  }

  public AuditLog(Long hospitalId, Long userId, String action, String entityType, Long entityId,
      String metadataJson, String ipAddress) {
    this.hospitalId = hospitalId;
    this.userId = userId;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.metadataJson = metadataJson;
    this.ipAddress = ipAddress;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public Long getUserId() { return userId; }
  public String getAction() { return action; }
  public String getEntityType() { return entityType; }
  public Long getEntityId() { return entityId; }
  public String getMetadataJson() { return metadataJson; }
  public String getIpAddress() { return ipAddress; }
  public Instant getCreatedAt() { return createdAt; }
}
