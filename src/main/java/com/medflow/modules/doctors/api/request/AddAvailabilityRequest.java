package com.medflow.modules.doctors.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Adds a consulting window: either a recurring weekday ({@code dayOfWeek}, 0 = Sunday) or
 * a one-off override for a {@code specificDate}. Exactly one of the two must be set.
 */
public record AddAvailabilityRequest(
    @Min(0) @Max(6) Integer dayOfWeek,
    LocalDate specificDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @Min(5) @Max(240) Integer slotDurationMinutes,
    Boolean available) {
}
