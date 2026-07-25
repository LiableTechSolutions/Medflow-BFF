package com.medflow.modules.users.api.response;

import com.medflow.modules.users.api.UserRole;
import com.medflow.modules.users.api.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserAccountResponse(
    UUID id,
    String fullName,
    String email,
    UserRole role,
    UserStatus status,
    Instant createdAt) {
}
