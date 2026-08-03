package com.medflow.modules.prescriptions.api.response;

/** One medication line, as stored inside {@code medicines_json}. */
public record PrescriptionItemResponse(
    String medicationName,
    String dosage,
    String frequency,
    Integer durationDays,
    String instructions) {
}
