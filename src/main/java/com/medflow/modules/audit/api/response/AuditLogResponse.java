package com.medflow.modules.audit.api.response;

import java.time.Instant;

public record AuditLogResponse(
    Long id,
    Long userId,
    String action,
    String entityType,
    Long entityId,
    String metadataJson,
    String ipAddress,
    Instant createdAt) {
}
