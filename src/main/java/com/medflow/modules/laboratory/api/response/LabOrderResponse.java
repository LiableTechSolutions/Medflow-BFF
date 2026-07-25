package com.medflow.modules.laboratory.api.response;

import com.medflow.modules.laboratory.api.LabOrderStatus;
import com.medflow.modules.laboratory.api.LabPriority;
import java.time.Instant;
import java.util.UUID;

public record LabOrderResponse(
    UUID id,
    UUID patientId,
    String patientName,
    UUID orderedBy,
    String orderedByName,
    String testName,
    LabPriority priority,
    LabOrderStatus status,
    String resultSummary,
    Instant orderedAt,
    Instant completedAt,
    Instant updatedAt) {
}
