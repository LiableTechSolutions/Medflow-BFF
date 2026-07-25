package com.medflow.shared.exception;

/** Raised when authentication fails; deliberately carries no detail about which factor failed. */
public class InvalidCredentialsException extends RuntimeException {
  public InvalidCredentialsException(String message) {
    super(message);
  }
}
