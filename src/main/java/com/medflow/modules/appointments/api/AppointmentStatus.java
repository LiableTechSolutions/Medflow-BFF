package com.medflow.modules.appointments.api;

/**
 * Mirrors {@code appointment_status_enum}. The happy path walks the front-desk workflow:
 * BOOKED → CONFIRMED → CHECKED_IN → IN_CONSULTATION → COMPLETED. Anything before
 * COMPLETED can end as CANCELLED, and a patient who never arrives ends as NO_SHOW.
 */
public enum AppointmentStatus {
  BOOKED, CONFIRMED, CHECKED_IN, IN_CONSULTATION, COMPLETED, CANCELLED, NO_SHOW;

  public boolean isClosed() {
    return this == COMPLETED || this == CANCELLED || this == NO_SHOW;
  }
}
