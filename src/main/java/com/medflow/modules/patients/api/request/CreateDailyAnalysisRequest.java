package com.medflow.modules.patients.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** A doctor's vitals/notes entry against the patient's active hospitalisation record. */
public record CreateDailyAnalysisRequest(
    @NotNull Long recordedByDoctorId,
    @Pattern(regexp = "\\d{2,3}/\\d{2,3}", message = "must look like 120/80")
    String bloodPressure,
    @Min(20) @Max(250) Integer pulse,
    BigDecimal temperature,
    @Min(0) @Max(100) Integer spo2,
    @Size(max = 2000) String notes) {
}
