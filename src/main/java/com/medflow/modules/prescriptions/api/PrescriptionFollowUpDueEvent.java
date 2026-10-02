package com.medflow.modules.prescriptions.api;

import java.time.LocalDate;

/** Raised by the daily follow-up scheduler, one day before {@code followUpDate}. */
public record PrescriptionFollowUpDueEvent(
    Long hospitalId,
    Long prescriptionId,
    String patientName,
    String patientPhone,
    String patientEmail,
    String doctorName,
    LocalDate followUpDate) {
}
