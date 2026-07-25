package com.medflow.shared.web;

import com.medflow.shared.api.*;
import com.medflow.shared.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestControllerAdvice
class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
    var errors = exception.getBindingResult().getFieldErrors().stream().map(e -> new ApiError(e.getField(), "VALIDATION_ERROR", e.getDefaultMessage())).toList();
    return response(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
  }
  @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ApiResponse<Void>> notFound(ResourceNotFoundException e, HttpServletRequest r) { return response(HttpStatus.NOT_FOUND, e.getMessage(), List.of(), r); }
  @ExceptionHandler(DuplicateResourceException.class) ResponseEntity<ApiResponse<Void>> conflict(DuplicateResourceException e, HttpServletRequest r) { return response(HttpStatus.CONFLICT, e.getMessage(), List.of(), r); }
  @ExceptionHandler(Exception.class) ResponseEntity<ApiResponse<Void>> unexpected(Exception e, HttpServletRequest r) { return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", List.of(), r); }
  private ResponseEntity<ApiResponse<Void>> response(HttpStatus s, String m, List<ApiError> e, HttpServletRequest r) { return ResponseEntity.status(s).body(ApiResponse.failure(m, e, r.getHeader("X-Trace-Id"))); }
}
