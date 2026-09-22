package com.medflow.modules.appointments.api;

import java.time.Instant;

/**
 * Published after a booking commits. Carries the tenant and display names so listeners
 * need no lookups and no security context of their own.
 */
public record AppointmentBookedEvent(
    Long hospitalId,
    /** A pre-signed token for the public queue board link — see QueueLinkTokenService. */
    String queueLinkToken,
    Long appointmentId,
    Long patientId,
    String patientName,
    String patientPhone,
    Long doctorId,
    String doctorName,
    Instant scheduledAt) {
}
