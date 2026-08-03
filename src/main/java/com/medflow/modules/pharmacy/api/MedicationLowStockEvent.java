package com.medflow.modules.pharmacy.api;

/** Published when an adjustment drops stock to or below the reorder level. */
public record MedicationLowStockEvent(
    Long hospitalId,
    Long medicationId,
    String name,
    int stockQuantity,
    int reorderLevel) {
}
