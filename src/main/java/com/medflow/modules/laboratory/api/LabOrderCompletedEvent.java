package com.medflow.modules.laboratory.api;

import java.util.UUID;

/** Published when results are signed off; consumed by the notifications module. */
public record LabOrderCompletedEvent(
    UUID labOrderId,
    UUID patientId,
    String patientName,
    String testName) {
}
