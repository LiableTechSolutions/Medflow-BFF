package com.medflow.modules.beds.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWardRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 40) String wardType,
    @NotNull @Min(0) @Max(200) Integer bedCount) {
}
