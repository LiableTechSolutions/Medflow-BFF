package com.medflow.modules.appointments.api.response;

import com.medflow.modules.appointments.api.AppointmentStatus;

/**
 * This appointment's live position among the doctor's still-active appointments for the
 * day (booked/confirmed/checked-in/in-consultation), ordered by queue number.
 * {@code position} is 1-based; {@code aheadCount} is how many are still ahead of it.
 * Once the appointment itself is closed (completed/cancelled/no-show), both are 0.
 */
public record QueueStatusResponse(
    Long appointmentId,
    Integer queueNumber,
    AppointmentStatus status,
    int position,
    int aheadCount,
    int totalActive) {
}
