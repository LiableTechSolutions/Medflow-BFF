package com.medflow.modules.appointments.api.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record BookAppointmentRequest(
    @NotNull UUID patientId,
    @NotNull UUID doctorId,
    @NotNull @Future Instant scheduledAt,
    @Min(5) @Max(240) Integer durationMinutes,
    @Size(max = 255) String reason,
    @Size(max = 1000) String notes) {
}
