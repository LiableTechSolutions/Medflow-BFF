package com.medflow.modules.laboratory.api.request;

import com.medflow.modules.laboratory.api.LabPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateLabOrderRequest(
    @NotNull UUID patientId,
    @NotNull UUID orderedBy,
    @NotBlank @Size(max = 150) String testName,
    @NotNull LabPriority priority) {
}
