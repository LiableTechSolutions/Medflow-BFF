package com.medflow.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(boolean success, String message, T data, List<ApiError> errors, Instant timestamp, String traceId) {
  public static <T> ApiResponse<T> success(String message, T data, String traceId) { return new ApiResponse<>(true, message, data, List.of(), Instant.now(), traceId); }
  public static ApiResponse<Void> failure(String message, List<ApiError> errors, String traceId) { return new ApiResponse<>(false, message, null, errors, Instant.now(), traceId); }
}
