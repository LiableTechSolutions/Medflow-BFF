package com.medflow.modules.rbac.api;

/**
 * Codes of the system roles seeded with the schema. They double as Spring Security role
 * names: a token carrying {@code ADMIN} satisfies {@code hasRole('ADMIN')}.
 */
public final class RoleCodes {

  public static final String ADMIN = "ADMIN";
  public static final String DOCTOR = "DOCTOR";
  public static final String NURSE = "NURSE";
  public static final String LAB_TECHNICIAN = "LAB_TECHNICIAN";
  public static final String PHARMACIST = "PHARMACIST";
  public static final String RECEPTIONIST = "RECEPTIONIST";
  public static final String PATIENT = "PATIENT";

  private RoleCodes() {
  }
}
