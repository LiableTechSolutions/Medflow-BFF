package com.medflow.modules.appointments.api.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Read-only queue board for one doctor's day — meant for a waiting-room TV or a link
 * shared with patients, so it carries no auth-only fields (no ids beyond queue number,
 * no contact details, no fees).
 */
public record PublicQueueBoardResponse(
    String hospitalName,
    String doctorName,
    String specialty,
    LocalDate date,
    Integer nowServingQueueNumber,
    String nowServingPatientName,
    int totalActive,
    List<PublicQueueEntry> upcoming) {
}
