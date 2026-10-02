package com.medflow.modules.prescriptions.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CreatePrescriptionRequest(
    @NotNull Long patientId,
    @NotNull Long doctorId,
    Long appointmentId,
    @Size(max = 2000) String diagnosis,
    /** Signing stamps the record; unsigned prescriptions are drafts. */
    Boolean digitallySigned,
    @NotEmpty List<@Valid PrescriptionItemRequest> medicines,
    /** When the patient should come back; a daily job reminds them the day before. */
    @FutureOrPresent LocalDate followUpDate) {
}
