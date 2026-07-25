package com.medflow.modules.patients.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatePatientRequest(
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @NotNull @Past LocalDate dateOfBirth,
    @NotBlank @Pattern(regexp = "MALE|FEMALE|OTHER") String gender,
    @Email @Size(max = 255) String email,
    @Size(max = 20) String phone,
    @Pattern(regexp = "(A|B|AB|O)[+-]", message = "must be a valid blood group such as O+ or AB-")
    String bloodGroup,
    @Size(max = 255) String address) {
}
