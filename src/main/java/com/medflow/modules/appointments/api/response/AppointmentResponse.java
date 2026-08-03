package com.medflow.modules.appointments.api.response;

import com.medflow.modules.appointments.api.AppointmentMode;
import com.medflow.modules.appointments.api.AppointmentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record AppointmentResponse(
    Long id,
    Long hospitalId,
    Long patientId,
    String patientName,
    Long doctorId,
    String doctorName,
    String doctorSpecialty,
    AppointmentMode appointmentMode,
    Instant scheduledAt,
    int durationMinutes,
    AppointmentStatus status,
    Integer queueNumber,
    Long bookedByUserId,
    String reason,
    BigDecimal consultationFee,
    String notes,
    Instant createdAt,
    Instant updatedAt) {
}
