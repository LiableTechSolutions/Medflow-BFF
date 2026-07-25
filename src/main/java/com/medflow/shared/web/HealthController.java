package com.medflow.shared.web;

import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.security.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PublicApi
class HealthController {

  @GetMapping("/health")
  @Operation(summary = "Service health", description = "Public liveness endpoint.")
  ApiResponse<Map<String, String>> health() {
    return ApiResponse.success("Service is healthy", Map.of("status", "UP"));
  }
}
