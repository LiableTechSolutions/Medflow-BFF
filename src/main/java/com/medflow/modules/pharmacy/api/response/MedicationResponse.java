package com.medflow.modules.pharmacy.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MedicationResponse(
    UUID id,
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
