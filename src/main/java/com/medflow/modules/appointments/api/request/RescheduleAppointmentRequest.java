package com.medflow.modules.appointments.api.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record RescheduleAppointmentRequest(@NotNull @Future Instant newScheduledAt) {
}
