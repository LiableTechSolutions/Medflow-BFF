package com.medflow.modules.settings.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSettingRequest(@NotBlank @Size(max = 1000) String value) {
}
