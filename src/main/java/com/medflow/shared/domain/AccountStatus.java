package com.medflow.shared.domain;

/**
 * Lifecycle shared by every principal-like record — hospitals, users, doctors and
 * patients. Mirrors {@code account_status_enum} in the database design; values are
 * persisted lower-case by {@link com.medflow.shared.persistence.AccountStatusConverter}.
 */
public enum AccountStatus {

  /** Fully usable. */
  ACTIVE,
  /** Archived: still visible in history, excluded from day-to-day lists. */
  INACTIVE,
  /** Blocked by an administrator; sign-in is refused. */
  SUSPENDED,
  /** Created but not yet verified (invited staff, self-registered patients). */
  PENDING_VERIFICATION;

  public boolean canSignIn() {
    return this == ACTIVE;
  }
}
