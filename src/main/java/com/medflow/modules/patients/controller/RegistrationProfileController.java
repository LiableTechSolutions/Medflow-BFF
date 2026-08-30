package com.medflow.modules.patients.controller;

import com.medflow.modules.patients.api.request.UpdateRegistrationProfileRequest;
import com.medflow.modules.patients.api.response.RegistrationProfileResponse;
import com.medflow.modules.patients.application.RegistrationProfileService;
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
@RequestMapping("/api/v1/patients")
class RegistrationProfileController {

  private final RegistrationProfileService service;
  private final TenantContext tenantContext;

  RegistrationProfileController(RegistrationProfileService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @GetMapping("/registration-profile")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get patient registration profile",
      description = "Returns the configured patient intake profile for this hospital.")
  ApiResponse<RegistrationProfileResponse> get() {
    return ApiResponse.success("Registration profile retrieved successfully",
        service.get(tenantContext.hospitalId()));
  }

  @PutMapping("/registration-profile")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update patient registration profile",
      description = "Updates the configured patient intake profile for this hospital.")
  ApiResponse<RegistrationProfileResponse> update(
      @Valid @RequestBody UpdateRegistrationProfileRequest request) {
    return ApiResponse.success("Registration profile saved successfully",
        service.update(tenantContext.hospitalId(), request));
  }
}
