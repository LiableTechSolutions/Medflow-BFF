package com.medflow.modules.patients.api.request;

import java.time.Instant;

/** {@code dischargeDate} defaults to now when omitted. */
public record DischargePatientRequest(Instant dischargeDate) {
}
