package com.medflow.modules.prescriptions.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PrescriptionItemRequest(
    @NotBlank @Size(max = 150) String medicationName,
    @NotBlank @Size(max = 50) String dosage,
    @NotBlank @Size(max = 50) String frequency,
    @Positive Integer durationDays,
    @Size(max = 255) String instructions) {
}
