package com.medflow.modules.prescriptions.api.response;

import java.util.UUID;

public record PrescriptionItemResponse(
    UUID id,
    String medicationName,
    String dosage,
    String frequency,
    Integer durationDays,
    String instructions) {
}
