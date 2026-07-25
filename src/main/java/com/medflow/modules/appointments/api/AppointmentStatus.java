package com.medflow.modules.appointments.api;

/** Lifecycle: PENDING → CONFIRMED → COMPLETED; PENDING/CONFIRMED may be CANCELLED. */
public enum AppointmentStatus {
  PENDING, CONFIRMED, COMPLETED, CANCELLED
}
