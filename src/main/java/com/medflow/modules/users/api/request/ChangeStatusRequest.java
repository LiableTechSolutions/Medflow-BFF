package com.medflow.modules.users.api.request;

import com.medflow.modules.users.api.UserStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull UserStatus status) {
}
