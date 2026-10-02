package com.medflow.modules.beds.api.request;

import jakarta.validation.constraints.NotNull;

public record AssignBedRequest(@NotNull Long patientId) {
}
