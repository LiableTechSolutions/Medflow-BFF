package com.medflow.modules.patients.api.response;

import java.math.BigDecimal;
import java.time.Instant;

public record DailyAnalysisResponse(
    Long id,
    Long hospitalisationRecordId,
    Long patientId,
    String bloodPressure,
    Integer pulse,
    BigDecimal temperature,
    Integer spo2,
    String notes,
    Long recordedByDoctorId,
    Instant recordedAt) {
}
