package com.medflow.modules.doctors.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateDoctorRequest(
    @NotBlank @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @Size(max = 20) String phone,
    @NotBlank @Size(max = 100) String specialty,
    @Size(max = 200) String qualification,
    @Min(0) Integer yearsOfExperience,
    @NotNull @DecimalMin("0.00") BigDecimal consultationFee,
    @Size(max = 255) String digitalSignatureUrl,
    @Size(max = 2000) String bio) {
}
