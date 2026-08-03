package com.medflow.modules.doctors.api.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorAvailabilityResponse(
    Long id,
    Long doctorId,
    Integer dayOfWeek,
    LocalDate specificDate,
    LocalTime startTime,
    LocalTime endTime,
    int slotDurationMinutes,
    boolean available) {
}
