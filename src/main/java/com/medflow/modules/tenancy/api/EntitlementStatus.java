package com.medflow.modules.tenancy.api;

/** Mirrors {@code entitlement_status_enum}; persisted lower-case. */
public enum EntitlementStatus {
  TRIAL, ACTIVE, EXPIRED, DISABLED;

  public boolean grantsAccess() {
    return this == TRIAL || this == ACTIVE;
  }
}
