package com.medflow.modules.users.api.request;

import com.medflow.shared.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull AccountStatus status) {
}
