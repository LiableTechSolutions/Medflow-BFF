package com.medflow.modules.doctors.api.response;

import com.medflow.modules.doctors.api.DoctorAvailability;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DoctorResponse(
    UUID id,
    String fullName,
    String email,
    String phone,
    String specialty,
    String department,
    String licenseNumber,
    DoctorAvailability availability,
    BigDecimal consultationFee,
    Instant createdAt,
    Instant updatedAt) {
}
