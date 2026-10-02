package com.medflow.modules.beds.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** How many beds to add to, or remove from, a ward. */
public record BedCountRequest(@NotNull @Min(1) @Max(200) Integer count) {
}
