package com.medflow.modules.tenancy.api.response;

import com.medflow.modules.tenancy.api.EntitlementStatus;
import java.time.Instant;

/** One row of the UI's module navigation: what it is, and whether this tenant may open it. */
public record ModuleEntitlementResponse(
    Integer moduleId,
    String moduleCode,
    String moduleName,
    int phase,
    String description,
    boolean generallyAvailable,
    EntitlementStatus status,
    boolean accessible,
    Instant activatedAt,
    Instant expiresAt) {
}
