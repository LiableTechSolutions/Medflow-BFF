/**
 * Pharmacy module: medication catalog, on-hand stock and dispensing adjustments.
 * Publishes {@code MedicationLowStockEvent} when stock crosses its reorder level so the
 * notifications module can raise a low-stock alert.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Pharmacy")
package com.medflow.modules.pharmacy;
