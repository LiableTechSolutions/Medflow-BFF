package com.medflow.modules.beds.api.response;

/** Hospital-wide occupancy, the numbers a live dashboard tile needs. */
public record BedSummaryResponse(
    int totalBeds,
    int availableBeds,
    int occupiedBeds,
    int maintenanceBeds,
    int occupancyPercent) {
}
