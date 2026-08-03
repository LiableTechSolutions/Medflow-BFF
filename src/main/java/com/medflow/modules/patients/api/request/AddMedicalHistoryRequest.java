package com.medflow.modules.patients.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddMedicalHistoryRequest(
    @NotBlank @Size(max = 200) String conditionName,
    @Size(max = 2000) String notes,
    Long recordedByDoctorId) {
}
