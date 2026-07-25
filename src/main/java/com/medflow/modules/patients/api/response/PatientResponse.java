package com.medflow.modules.patients.api.response;

import com.medflow.modules.patients.api.PatientStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
    UUID id,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String gender,
    String email,
    String phone,
    String bloodGroup,
    String address,
    PatientStatus status,
    Instant createdAt,
    Instant updatedAt) {
}
