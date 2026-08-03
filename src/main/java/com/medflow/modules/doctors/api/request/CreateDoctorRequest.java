package com.medflow.modules.doctors.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Onboards a doctor. The account is created alongside the profile; when no password is
 * supplied a random one is set and the doctor completes sign-up through "forgot password".
 */
public record CreateDoctorRequest(
    @NotBlank @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @NotBlank @Email @Size(max = 120) String email,
    @Size(max = 20) String phone,
    @Size(min = 8, max = 72) String password,
    @NotBlank @Size(max = 100) String specialty,
    @Size(max = 200) String qualification,
    @NotBlank @Size(max = 100) String registrationNumber,
    @Min(0) Integer yearsOfExperience,
    @NotNull @DecimalMin("0.00") BigDecimal consultationFee,
    @Size(max = 2000) String bio) {
}
