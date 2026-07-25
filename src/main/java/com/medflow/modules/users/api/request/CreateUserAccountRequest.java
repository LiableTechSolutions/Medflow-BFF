package com.medflow.modules.users.api.request;

import com.medflow.modules.users.api.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserAccountRequest(
    @NotBlank @Size(max = 150) String fullName,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(min = 8, max = 72) String rawPassword,
    UserRole role) {
}
