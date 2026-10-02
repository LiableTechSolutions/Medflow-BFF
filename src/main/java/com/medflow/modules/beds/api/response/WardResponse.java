package com.medflow.modules.beds.api.response;

public record WardResponse(
    Long id,
    String name,
    String wardType,
    int totalBeds,
    int availableBeds,
    int occupiedBeds,
    int maintenanceBeds) {
}
