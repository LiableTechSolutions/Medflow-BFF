package com.medflow.modules.notifications.domain.entity;

import com.medflow.modules.notifications.api.NotificationCategory;
import com.medflow.modules.notifications.api.NotificationSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Hospital-level notification. Per-recipient targeting and read receipts are a deliberate
 * later step; the current feed is shared by the whole clinic, matching the single feed
 * shown in the UI.
 */
@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationSeverity severity;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(nullable = false, length = 500)
  private String message;

  @Column(name = "is_read", nullable = false)
  private boolean read;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Notification() {
  }

  public Notification(Long hospitalId, NotificationCategory category,
      NotificationSeverity severity, String title, String message) {
    this.hospitalId = hospitalId;
    this.category = category;
    this.severity = severity;
    this.title = title;
    this.message = message;
    this.read = false;
    this.createdAt = Instant.now();
  }

  public void markRead() {
    this.read = true;
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public NotificationCategory getCategory() { return category; }
  public NotificationSeverity getSeverity() { return severity; }
  public String getTitle() { return title; }
  public String getMessage() { return message; }
  public boolean isRead() { return read; }
  public Instant getCreatedAt() { return createdAt; }
}
