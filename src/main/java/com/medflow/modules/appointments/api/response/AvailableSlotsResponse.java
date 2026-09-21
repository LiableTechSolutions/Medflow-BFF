package com.medflow.modules.appointments.api.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Open consulting slots for a doctor on a given day, after subtracting existing bookings. */
public record AvailableSlotsResponse(Long doctorId, LocalDate date, List<Instant> slots) {
}
