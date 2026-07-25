package com.medflow.shared.web;

import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.security.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @PublicApi
class HealthController {
  @GetMapping("/health") @Operation(summary = "Service health", description = "Public liveness endpoint.")
  ApiResponse<Map<String, String>> health(HttpServletRequest request) { return ApiResponse.success("Service is healthy", Map.of("status", "UP"), request.getHeader("X-Trace-Id")); }
}
