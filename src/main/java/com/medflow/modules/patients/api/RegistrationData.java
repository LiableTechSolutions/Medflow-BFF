package com.medflow.modules.patients.api;

import java.util.Map;

/** Canonical patient registration values. Keys are the registration field catalogue names. */
public record RegistrationData(Map<String, Object> values) {
  public Object value(String field) {
    return values == null ? null : values.get(field);
  }
}
