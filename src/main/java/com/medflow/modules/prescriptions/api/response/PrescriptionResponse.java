package com.medflow.modules.prescriptions.api.response;

import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import java.time.Instant;
import java.util.List;

public record PrescriptionResponse(
    Long id,
    Long hospitalId,
    Long appointmentId,
    Long patientId,
    String patientName,
    Long doctorId,
    String doctorName,
    String diagnosis,
    List<PrescriptionItemResponse> medicines,
    boolean digitallySigned,
    Instant signedAt,
    PrescriptionStatus status,
    Instant createdAt,
    Instant updatedAt) {
}
