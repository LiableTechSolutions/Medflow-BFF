package com.medflow.shared.api;

import java.time.Instant;
import java.util.List;
import org.slf4j.MDC;

/**
 * Uniform response envelope for every REST endpoint. The trace identifier is taken from
 * the MDC populated by {@code TraceIdFilter}, so controllers never handle it directly.
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    List<ApiError> errors,
    Instant timestamp,
    String traceId) {

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data, List.of(), Instant.now(), MDC.get("traceId"));
  }

  public static ApiResponse<Void> failure(String message, List<ApiError> errors) {
    return new ApiResponse<>(false, message, null, errors, Instant.now(), MDC.get("traceId"));
  }
}
