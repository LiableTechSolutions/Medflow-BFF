package com.medflow.modules.pharmacy.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdjustStockRequest(
    @NotNull Integer delta,
    @Size(max = 255) String reason) {
}
