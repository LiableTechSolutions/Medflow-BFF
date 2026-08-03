package com.medflow.modules.laboratory.api.request;

import com.medflow.modules.laboratory.api.LabPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLabOrderRequest(
    @NotNull Long patientId,
    @NotNull Long doctorId,
    @NotBlank @Size(max = 150) String testName,
    @NotNull LabPriority priority) {
}
