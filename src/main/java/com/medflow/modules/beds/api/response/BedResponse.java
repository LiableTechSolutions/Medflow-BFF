package com.medflow.modules.beds.api.response;

import com.medflow.modules.beds.api.BedStatus;
import java.time.Instant;

public record BedResponse(
    Long id,
    Long wardId,
    String bedNumber,
    BedStatus status,
    Long patientId,
    String patientName,
    Instant occupiedAt) {
}
