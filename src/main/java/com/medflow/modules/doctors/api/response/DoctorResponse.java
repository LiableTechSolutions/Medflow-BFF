package com.medflow.modules.doctors.api.response;

import com.medflow.shared.domain.AccountStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record DoctorResponse(
    Long id,
    Long hospitalId,
    Long userId,
    String doctorCode,
    String firstName,
    String lastName,
    String fullName,
    String email,
    String phone,
    String specialty,
    String qualification,
    String registrationNumber,
    Integer yearsOfExperience,
    BigDecimal consultationFee,
    String digitalSignatureUrl,
    String bio,
    AccountStatus status,
    Instant createdAt,
    Instant updatedAt) {
}
