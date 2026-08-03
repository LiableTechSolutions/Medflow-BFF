package com.medflow.modules.users.api.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeRoleRequest(@NotBlank String roleCode) {
}
