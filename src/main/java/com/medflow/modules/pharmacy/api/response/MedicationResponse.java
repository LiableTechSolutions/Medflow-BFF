package com.medflow.modules.pharmacy.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record MedicationResponse(
    Long id,
    Long hospitalId,
    String name,
    String category,
    BigDecimal unitPrice,
    int stockQuantity,
    int reorderLevel,
    boolean lowStock,
    LocalDate expiryDate,
    Instant createdAt,
    Instant updatedAt) {
}
