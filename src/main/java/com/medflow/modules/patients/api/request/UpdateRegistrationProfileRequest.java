package com.medflow.modules.patients.api.request;

import com.medflow.modules.patients.api.RegistrationProfileType;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record UpdateRegistrationProfileRequest(
    @NotNull RegistrationProfileType startingProfile,
    @NotNull Map<String, String> fieldStates) {
}
