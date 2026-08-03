package com.medflow.modules.users.api.response;

import com.medflow.shared.domain.AccountStatus;
import com.medflow.shared.domain.Gender;
import java.time.Instant;
import java.time.LocalDate;

public record UserAccountResponse(
    Long id,
    Long hospitalId,
    String userUid,
    String firstName,
    String lastName,
    String fullName,
    String email,
    String phone,
    Integer roleId,
    String roleCode,
    String roleName,
    Integer userGroupId,
    Gender gender,
    LocalDate dateOfBirth,
    String profilePhotoUrl,
    AccountStatus status,
    Instant lastLoginAt,
    Instant createdAt,
    Instant updatedAt) {
}
