package com.medflow.modules.appointments.api.response;

import com.medflow.modules.appointments.api.AppointmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(
    UUID id,
    UUID patientId,
    String patientName,
    UUID doctorId,
    String doctorName,
    Instant scheduledAt,
    int durationMinutes,
    String reason,
    AppointmentStatus status,
    BigDecimal consultationFee,
    String notes,
    Instant createdAt,
    Instant updatedAt) {
}
