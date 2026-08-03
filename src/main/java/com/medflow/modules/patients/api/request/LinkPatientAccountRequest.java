package com.medflow.modules.patients.api.request;

import com.medflow.modules.patients.api.MappingRelation;
import jakarta.validation.constraints.NotNull;

/** Grants a portal account the right to act for a patient. */
public record LinkPatientAccountRequest(
    @NotNull Long userId,
    @NotNull MappingRelation relation,
    Boolean primaryContact) {
}
