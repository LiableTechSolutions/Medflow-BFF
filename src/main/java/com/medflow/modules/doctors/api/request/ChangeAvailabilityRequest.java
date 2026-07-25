package com.medflow.modules.doctors.api.request;

import com.medflow.modules.doctors.api.DoctorAvailability;
import jakarta.validation.constraints.NotNull;

public record ChangeAvailabilityRequest(@NotNull DoctorAvailability availability) {
}
