package com.medflow.modules.tenancy.api.response;

import com.medflow.shared.domain.AccountStatus;
import java.time.Instant;

public record HospitalResponse(
    Long id,
    String hospitalCode,
    String name,
    String legalName,
    String hospitalType,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String country,
    String pincode,
    String phone,
    String email,
    String timezone,
    AccountStatus status,
    Instant onboardedAt,
    Instant createdAt,
    Instant updatedAt) {
}
