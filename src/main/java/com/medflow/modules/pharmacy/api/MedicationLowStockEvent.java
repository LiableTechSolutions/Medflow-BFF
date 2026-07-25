package com.medflow.modules.pharmacy.api;

import java.util.UUID;

/** Published when an adjustment drops stock to or below the reorder level. */
public record MedicationLowStockEvent(
    UUID medicationId,
    String name,
    int stockQuantity,
    int reorderLevel) {
}
