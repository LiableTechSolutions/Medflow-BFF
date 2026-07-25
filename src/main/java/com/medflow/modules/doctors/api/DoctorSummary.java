package com.medflow.modules.doctors.api;

import java.math.BigDecimal;
import java.util.UUID;

/** Projection consumed by scheduling modules; includes the fee captured at booking time. */
public record DoctorSummary(UUID id, String fullName, String specialty, BigDecimal consultationFee) {
}
