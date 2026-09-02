package com.medflow.modules.settings.api.response;

import com.medflow.modules.settings.api.RegistrationFieldState;
import java.time.Instant;
import java.util.Map;

public record RegistrationProfileResponse(
    String template, Map<String, RegistrationFieldState> fieldStates, Instant updatedAt) {
}
