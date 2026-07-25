package com.medflow.modules.prescriptions.api.response;

import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PrescriptionResponse(
    UUID id,
    UUID patientId,
    String patientName,
    UUID doctorId,
    String doctorName,
    PrescriptionStatus status,
    String notes,
    List<PrescriptionItemResponse> items,
    Instant issuedAt,
    Instant updatedAt) {
}
