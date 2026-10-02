package com.medflow.modules.patients.api.response;

import com.medflow.modules.patients.api.HospitalisationStatus;
import java.time.Instant;

public record HospitalisationRecordResponse(
    Long id,
    Long patientId,
    Long hospitalId,
    Instant admissionDate,
    Instant dischargeDate,
    String ward,
    String bed,
    Long admittingDoctorId,
    HospitalisationStatus status,
    Instant createdAt,
    Instant updatedAt) {
}
