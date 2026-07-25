package com.medflow.modules.laboratory.api;

/** Lifecycle: ORDERED → IN_PROGRESS → COMPLETED; ORDERED/IN_PROGRESS may be CANCELLED. */
public enum LabOrderStatus {
  ORDERED, IN_PROGRESS, COMPLETED, CANCELLED
}
