package com.medflow.modules.patients.api.response;

import com.medflow.modules.patients.api.RegistrationProfileType;
import java.time.Instant;
import java.util.Map;

public record RegistrationProfileResponse(
    Long id,
    Long hospitalId,
    RegistrationProfileType startingProfile,
    Map<String, String> fieldStates,
    Instant updatedAt) {
}
