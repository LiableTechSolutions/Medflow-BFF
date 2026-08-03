package com.medflow.modules.users.api.request;

import com.medflow.shared.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Creates a staff account. {@code roleCode} defaults to RECEPTIONIST and
 * {@code userGroupCode} to the staff portal when omitted.
 */
public record CreateUserAccountRequest(
    @NotBlank @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @NotBlank @Email @Size(max = 120) String email,
    @Size(max = 20) String phone,
    @NotBlank @Size(min = 8, max = 72) String rawPassword,
    String roleCode,
    String userGroupCode,
    Gender gender,
    @Past LocalDate dateOfBirth) {
}
