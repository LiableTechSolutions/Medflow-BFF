package com.medflow.modules.laboratory.api.response;

import com.medflow.modules.laboratory.api.LabOrderStatus;
import com.medflow.modules.laboratory.api.LabPriority;
import java.time.Instant;

public record LabOrderResponse(
    Long id,
    Long hospitalId,
    Long patientId,
    String patientName,
    Long doctorId,
    String doctorName,
    String testName,
    LabPriority priority,
    LabOrderStatus status,
    String resultSummary,
    Instant orderedAt,
    Instant completedAt,
    Instant updatedAt) {
}
