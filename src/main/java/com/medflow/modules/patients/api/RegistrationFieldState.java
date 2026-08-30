package com.medflow.modules.patients.api;

public enum RegistrationFieldState {
  REQUIRED,
  OPTIONAL,
  HIDDEN;

  public static RegistrationFieldState from(String value) {
    if (value == null) {
      return null;
    }
    return switch (value.trim().toUpperCase()) {
      case "REQUIRED" -> REQUIRED;
      case "OPTIONAL" -> OPTIONAL;
      case "HIDDEN" -> HIDDEN;
      default -> throw new IllegalArgumentException("Unsupported field state: " + value);
    };
  }
}
