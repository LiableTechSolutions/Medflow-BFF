package com.medflow.modules.settings.controller;

import com.medflow.modules.settings.api.SettingsService;
import com.medflow.modules.settings.api.request.UpdateSettingRequest;
import com.medflow.modules.settings.api.response.SettingResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
@Validated
class SettingsController {

  private static final String KEY_PATTERN = "[a-z0-9]+([._-][a-z0-9]+)*";

  private final SettingsService service;
  private final TenantContext tenantContext;

  SettingsController(SettingsService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @GetMapping
  @Operation(summary = "List settings", description = "All settings for this hospital, sorted by key.")
  ApiResponse<List<SettingResponse>> getAll() {
    return ApiResponse.success("Settings retrieved successfully",
        service.getAll(tenantContext.hospitalId()));
  }

  @GetMapping("/{key}")
  @Operation(summary = "Get setting", description = "Returns one setting by key.")
  ApiResponse<SettingResponse> get(@PathVariable @Pattern(regexp = KEY_PATTERN) String key) {
    return ApiResponse.success("Setting retrieved successfully",
        service.get(tenantContext.hospitalId(), key));
  }

  @PutMapping("/{key}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Upsert setting",
      description = "Admin-only: creates or replaces a setting value.")
  ApiResponse<SettingResponse> put(@PathVariable @Pattern(regexp = KEY_PATTERN) String key,
      @Valid @RequestBody UpdateSettingRequest request) {
    return ApiResponse.success("Setting saved successfully",
        service.put(tenantContext.hospitalId(), key, request));
  }
}
