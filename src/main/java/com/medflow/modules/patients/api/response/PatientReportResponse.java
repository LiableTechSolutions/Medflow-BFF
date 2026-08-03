package com.medflow.modules.patients.api.response;

import java.time.Instant;

public record PatientReportResponse(
    Long id,
    Long patientId,
    String reportType,
    String fileUrl,
    Long uploadedByUserId,
    Instant uploadedAt) {
}
