package com.medflow.modules.appointments.api;

import java.time.Instant;
import java.util.UUID;

/** Published after a booking commits; carries display names so listeners need no lookups. */
public record AppointmentBookedEvent(
    UUID appointmentId,
    UUID patientId,
    String patientName,
    UUID doctorId,
    String doctorName,
    Instant scheduledAt) {
}
