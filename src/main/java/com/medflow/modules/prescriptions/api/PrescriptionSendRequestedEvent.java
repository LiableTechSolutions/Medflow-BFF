package com.medflow.modules.prescriptions.api;

/**
 * Staff explicitly asked to send this prescription to the patient (view/print alone never
 * triggers a send). The notifications module reacts to this; it never calls back here.
 */
public record PrescriptionSendRequestedEvent(
    Long hospitalId,
    Long prescriptionId,
    String patientName,
    String patientPhone,
    String patientEmail,
    String doctorName,
    String diagnosis,
    String medicinesSummary) {
}
