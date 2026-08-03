package com.medflow.modules.patients.api.request;

import com.medflow.shared.domain.AccountStatus;
import com.medflow.shared.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdatePatientRequest(
    @NotBlank @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    Gender gender,
    @Past LocalDate dateOfBirth,
    @Pattern(regexp = "(A|B|AB|O)[+-]", message = "must be a valid blood group such as O+ or AB-")
    String bloodGroup,
    @Size(max = 20) String phone,
    @Email @Size(max = 120) String email,
    @Size(max = 1000) String address,
    @Size(max = 100) String emergencyContactName,
    @Size(max = 20) String emergencyContactPhone,
    @NotNull AccountStatus status) {
}
