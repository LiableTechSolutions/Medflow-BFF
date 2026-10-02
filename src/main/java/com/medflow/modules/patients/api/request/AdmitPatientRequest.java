package com.medflow.modules.patients.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Admission details for a hospitalisation record. {@code admissionDate} defaults to now. */
public record AdmitPatientRequest(
    @NotNull Long admittingDoctorId,
    @NotBlank @Size(max = 50) String ward,
    @Size(max = 20) String bed,
    @PastOrPresent Instant admissionDate) {
}
