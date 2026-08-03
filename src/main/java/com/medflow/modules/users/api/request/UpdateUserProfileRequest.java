package com.medflow.modules.users.api.request;

import com.medflow.shared.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateUserProfileRequest(
    @NotBlank @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @Size(max = 20) String phone,
    Gender gender,
    @Past LocalDate dateOfBirth,
    @Size(max = 255) String profilePhotoUrl) {
}
