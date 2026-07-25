package com.medflow.modules.prescriptions.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreatePrescriptionRequest(
    @NotNull UUID patientId,
    @NotNull UUID doctorId,
    @Size(max = 1000) String notes,
    @NotEmpty List<@Valid PrescriptionItemRequest> items) {
}
