package com.medflow.shared.exception;

/** Raised when a request is syntactically valid but violates a domain invariant (HTTP 422). */
public class BusinessRuleViolationException extends RuntimeException {
  public BusinessRuleViolationException(String message) {
    super(message);
  }
}
