package com.medflow.modules.prescriptions.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** Full replacement of the editable parts; a null follow-up date clears the reminder. */
public record UpdatePrescriptionRequest(
    @Size(max = 2000) String diagnosis,
    Boolean digitallySigned,
    @NotEmpty List<@Valid PrescriptionItemRequest> medicines,
    @FutureOrPresent LocalDate followUpDate) {
}
