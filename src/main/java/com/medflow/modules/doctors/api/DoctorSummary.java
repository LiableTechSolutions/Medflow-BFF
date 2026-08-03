package com.medflow.modules.doctors.api;

import java.math.BigDecimal;

/** Projection consumed by scheduling modules; includes the fee captured at booking time. */
public record DoctorSummary(
    Long id,
    Long hospitalId,
    Long userId,
    String doctorCode,
    String fullName,
    String specialty,
    BigDecimal consultationFee) {
}
