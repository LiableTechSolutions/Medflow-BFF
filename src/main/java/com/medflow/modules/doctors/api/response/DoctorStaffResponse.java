package com.medflow.modules.doctors.api.response;

import java.time.Instant;

public record DoctorStaffResponse(
    Long id,
    Long userId,
    String userFullName,
    String userEmail,
    String relationType,
    boolean primary,
    Instant createdAt) {
}
