package com.medflow.modules.patients.api;

/** Lightweight projection other modules use to render patient names without a join. */
public record PatientSummary(
    Long id,
    Long hospitalId,
    String patientCode,
    String fullName,
    String phone) {
}
