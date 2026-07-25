package com.medflow.modules.pharmacy.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateMedicationRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Size(max = 100) String category,
    @NotNull @DecimalMin(value = "0.00") BigDecimal unitPrice,
    @NotNull @PositiveOrZero Integer reorderLevel,
    LocalDate expiryDate) {
}
