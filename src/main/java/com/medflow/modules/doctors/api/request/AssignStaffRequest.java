package com.medflow.modules.doctors.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Attaches a staff account to a doctor, e.g. {@code front_desk} or {@code nurse}. */
public record AssignStaffRequest(
    @NotNull Long userId,
    @Size(max = 50) String relationType,
    Boolean primary) {
}
