package com.medflow.modules.tenancy.controller;

import com.medflow.modules.tenancy.api.HospitalService;
import com.medflow.modules.tenancy.api.ModuleCatalogService;
import com.medflow.modules.tenancy.api.request.UpdateHospitalRequest;
import com.medflow.modules.tenancy.api.response.HospitalResponse;
import com.medflow.modules.tenancy.api.response.ModuleEntitlementResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The caller's own hospital. There is deliberately no "list all hospitals" endpoint —
 * a tenant may only ever see itself, and the tenant is taken from the token.
 */
@RestController
@RequestMapping("/api/v1/hospital")
class HospitalController {

  private final HospitalService hospitalService;
  private final ModuleCatalogService moduleCatalogService;
  private final TenantContext tenantContext;

  HospitalController(HospitalService hospitalService, ModuleCatalogService moduleCatalogService,
      TenantContext tenantContext) {
    this.hospitalService = hospitalService;
    this.moduleCatalogService = moduleCatalogService;
    this.tenantContext = tenantContext;
  }

  @GetMapping
  @Operation(summary = "Get hospital profile",
      description = "Returns the profile of the workspace the caller belongs to.")
  ApiResponse<HospitalResponse> profile() {
    return ApiResponse.success("Hospital retrieved successfully",
        hospitalService.findById(tenantContext.hospitalId()));
  }

  @PutMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update hospital profile",
      description = "Admin-only: updates the workspace's legal and contact details.")
  ApiResponse<HospitalResponse> update(@Valid @RequestBody UpdateHospitalRequest request) {
    return ApiResponse.success("Hospital updated successfully",
        hospitalService.update(tenantContext.hospitalId(), request));
  }

  @GetMapping("/modules")
  @Operation(summary = "List module entitlements",
      description = "The product modules this hospital is licensed for; drives the UI navigation.")
  ApiResponse<List<ModuleEntitlementResponse>> modules() {
    return ApiResponse.success("Modules retrieved successfully",
        moduleCatalogService.forHospital(tenantContext.hospitalId()));
  }
}
