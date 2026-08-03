package com.medflow.modules.auth.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Signing up creates a workspace: a hospital plus its first administrator. The UI's
 * signup form collects a single full name, so it is split on the first space.
 * {@code hospitalName} is optional — omitted, the workspace is named after the signer.
 */
public record RegisterRequest(
    @NotBlank @Size(max = 150) String fullName,
    @NotBlank @Email @Size(max = 120) String email,
    @NotBlank @Size(min = 8, max = 72) String password,
    @NotBlank String confirmPassword,
    @Size(max = 200) String hospitalName,
    @Size(max = 20) String phone) {
}
