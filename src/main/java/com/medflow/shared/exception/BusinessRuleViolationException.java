package com.medflow.shared.exception;

import com.medflow.shared.api.ApiError;
import java.util.List;

/** Raised when a request is syntactically valid but violates a domain invariant (HTTP 422). */
public class BusinessRuleViolationException extends RuntimeException {
  private final List<ApiError> errors;

  public BusinessRuleViolationException(String message) {
    this(message, List.of());
  }

  public BusinessRuleViolationException(String message, List<ApiError> errors) {
    super(message);
    this.errors = errors;
  }

  public List<ApiError> errors() { return errors; }
}
