package com.medflow.modules.doctors.api.request;

import com.medflow.shared.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeDoctorStatusRequest(@NotNull AccountStatus status) {
}
