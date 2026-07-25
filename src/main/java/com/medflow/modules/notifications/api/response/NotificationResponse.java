package com.medflow.modules.notifications.api.response;

import com.medflow.modules.notifications.api.NotificationCategory;
import com.medflow.modules.notifications.api.NotificationSeverity;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    NotificationCategory category,
    NotificationSeverity severity,
    String title,
    String message,
    boolean read,
    Instant createdAt) {
}
