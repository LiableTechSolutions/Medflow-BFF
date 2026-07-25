package com.medflow.modules.doctors.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateDoctorRequest(
    @NotBlank @Size(max = 150) String fullName,
    @NotBlank @Email @Size(max = 255) String email,
    @Size(max = 20) String phone,
    @NotBlank @Size(max = 100) String specialty,
    @NotBlank @Size(max = 100) String department,
    @NotNull @DecimalMin(value = "0.00") BigDecimal consultationFee) {
}
