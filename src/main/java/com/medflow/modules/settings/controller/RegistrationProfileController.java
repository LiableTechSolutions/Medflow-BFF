package com.medflow.modules.settings.controller;

import com.medflow.modules.settings.api.RegistrationProfileService;
import com.medflow.modules.settings.api.request.UpdateRegistrationProfileRequest;
import com.medflow.modules.settings.api.response.RegistrationProfileResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings/patient-registration")
class RegistrationProfileController {
  private final RegistrationProfileService service;
  private final TenantContext tenantContext;

  RegistrationProfileController(RegistrationProfileService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @GetMapping
  @Operation(summary = "Get patient registration profile")
  ApiResponse<RegistrationProfileResponse> get() {
    return ApiResponse.success("Patient registration profile retrieved successfully",
        service.get(tenantContext.hospitalId()));
  }

  @PutMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update patient registration profile")
  ApiResponse<RegistrationProfileResponse> update(
      @Valid @RequestBody UpdateRegistrationProfileRequest request) {
    return ApiResponse.success("Patient registration profile saved successfully",
        service.update(tenantContext.hospitalId(), tenantContext.userId(), request));
  }
}
