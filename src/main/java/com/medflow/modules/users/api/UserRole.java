package com.medflow.modules.users.api;

/** Workspace roles; the JWT carries one of these and method security enforces them. */
public enum UserRole {
  ADMIN, DOCTOR, NURSE, LAB_TECHNICIAN, PHARMACIST, RECEPTIONIST
}
