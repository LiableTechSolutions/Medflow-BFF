package com.medflow.modules.prescriptions.api;

/** Lifecycle of a prescription; persisted lower-case alongside the reference columns. */
public enum PrescriptionStatus {
  ACTIVE, COMPLETED, CANCELLED
}
