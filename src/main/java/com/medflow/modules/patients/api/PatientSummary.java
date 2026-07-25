package com.medflow.modules.patients.api;

import java.util.UUID;

/** Lightweight projection other modules use to render patient names without a join. */
public record PatientSummary(UUID id, String fullName) {
}
