package com.medflow.modules.patients.api;

/** Raised when a patient's hospital stay ends, so other modules can release what they held. */
public record PatientDischargedEvent(Long hospitalId, Long patientId) {
}
