package com.medflow.modules.beds.api.request;

import jakarta.validation.constraints.NotNull;

public record MaintenanceRequest(@NotNull Boolean underMaintenance) {
}
