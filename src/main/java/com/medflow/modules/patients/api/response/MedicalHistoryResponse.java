package com.medflow.modules.patients.api.response;

import java.time.Instant;

public record MedicalHistoryResponse(
    Long id,
    Long patientId,
    String conditionName,
    String notes,
    Long recordedByDoctorId,
    Instant recordedAt) {
}
