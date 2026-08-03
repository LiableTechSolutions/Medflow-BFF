package com.medflow.modules.laboratory.api;

/** Published when results are signed off; consumed by the notifications module. */
public record LabOrderCompletedEvent(
    Long hospitalId,
    Long labOrderId,
    Long patientId,
    String patientName,
    String testName) {
}
