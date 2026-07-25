package com.medflow.modules.users.api.request;

import com.medflow.modules.users.api.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull UserRole role) {
}
