package com.medflow.modules.patients.api;

public enum RegistrationProfileType {
  BASIC,
  COMPREHENSIVE;

  public static RegistrationProfileType from(String value) {
    if (value == null) {
      return null;
    }
    return switch (value.trim().toUpperCase()) {
      case "BASIC" -> BASIC;
      case "COMPREHENSIVE" -> COMPREHENSIVE;
      default -> throw new IllegalArgumentException("Unsupported profile type: " + value);
    };
  }
}
