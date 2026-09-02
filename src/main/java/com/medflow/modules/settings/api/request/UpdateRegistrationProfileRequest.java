package com.medflow.modules.settings.api.request;

import com.medflow.modules.settings.api.RegistrationFieldState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record UpdateRegistrationProfileRequest(
    @NotBlank String template,
    @NotNull Map<String, RegistrationFieldState> fieldStates) {
}
