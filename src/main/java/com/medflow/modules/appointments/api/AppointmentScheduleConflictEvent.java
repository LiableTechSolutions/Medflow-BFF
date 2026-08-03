package com.medflow.modules.appointments.api;

import java.time.Instant;

/**
 * Raised when a booking overlaps an existing one for the same doctor. Bookings are
 * flagged rather than blocked — front-desk staff resolve conflicts from the notifications
 * feed (mirrors the UI's "Schedule conflict" alert).
 */
public record AppointmentScheduleConflictEvent(
    Long hospitalId,
    Long doctorId,
    String doctorName,
    Instant scheduledAt) {
}
