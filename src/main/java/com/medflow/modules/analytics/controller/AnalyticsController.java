package com.medflow.modules.analytics.controller;

import com.medflow.modules.analytics.api.AnalyticsService;
import com.medflow.modules.analytics.api.response.DailyActivityResponse;
import com.medflow.modules.analytics.api.response.DashboardSummaryResponse;
import com.medflow.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Validated
class AnalyticsController {

  private final AnalyticsService service;

  AnalyticsController(AnalyticsService service) {
    this.service = service;
  }

  @GetMapping("/dashboard")
  @Operation(summary = "Dashboard summary", description = "KPI cards: revenue MTD, patients, doctors, appointments today, lab reports, active cases.")
  ApiResponse<DashboardSummaryResponse> dashboard() {
    return ApiResponse.success("Dashboard summary retrieved successfully",
        service.getDashboardSummary());
  }

  @GetMapping("/activity")
  @Operation(summary = "Activity series", description = "Daily visit counts for the activity chart.")
  ApiResponse<List<DailyActivityResponse>> activity(
      @RequestParam(defaultValue = "7") @Min(1) @Max(31) int days) {
    return ApiResponse.success("Activity retrieved successfully", service.getActivity(days));
  }
}
