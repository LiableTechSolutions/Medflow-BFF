package com.medflow.modules.laboratory.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteLabOrderRequest(@NotBlank @Size(max = 2000) String resultSummary) {
}
