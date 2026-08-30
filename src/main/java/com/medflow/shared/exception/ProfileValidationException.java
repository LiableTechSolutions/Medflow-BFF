package com.medflow.shared.exception;

import com.medflow.shared.api.ApiError;
import java.util.List;

public class ProfileValidationException extends RuntimeException {
  private final List<ApiError> errors;
  public ProfileValidationException(String message, List<ApiError> errors) { super(message); this.errors = errors; }
  public List<ApiError> getErrors() { return errors; }
}
