package com.medflow.modules.appointments.api.response;

import com.medflow.modules.appointments.api.AppointmentStatus;
import java.time.Instant;

/** One row on the public/TV queue board — deliberately narrow: no contact details, fee or notes. */
public record PublicQueueEntry(
    Integer queueNumber,
    String patientName,
    AppointmentStatus status,
    Instant scheduledAt) {
}
