package com.medflow.modules.beds.controller;

import com.medflow.modules.beds.api.BedService;
import com.medflow.modules.beds.api.request.AssignBedRequest;
import com.medflow.modules.beds.api.request.BedCountRequest;
import com.medflow.modules.beds.api.request.CreateWardRequest;
import com.medflow.modules.beds.api.request.MaintenanceRequest;
import com.medflow.modules.beds.api.response.BedResponse;
import com.medflow.modules.beds.api.response.BedSummaryResponse;
import com.medflow.modules.beds.api.response.WardResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Any signed-in staff can read; changing capacity is admin-only, occupancy is ward staff. */
@RestController
@RequestMapping("/api/v1/beds")
class BedController {

  private static final String WARD_STAFF = "hasAnyRole('ADMIN', 'NURSE')";

  private final BedService service;
  private final TenantContext tenantContext;

  BedController(BedService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @GetMapping("/summary")
  @Operation(summary = "Bed occupancy summary", description = "Hospital-wide totals for the dashboard.")
  ApiResponse<BedSummaryResponse> summary() {
    return ApiResponse.success("Bed summary retrieved successfully",
        service.summary(tenantContext.hospitalId()));
  }

  @GetMapping("/wards")
  @Operation(summary = "List wards", description = "Each ward with its bed counts.")
  ApiResponse<List<WardResponse>> wards() {
    return ApiResponse.success("Wards retrieved successfully", service.wards(tenantContext.hospitalId()));
  }

  @PostMapping("/wards")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create ward", description = "Admin-only: creates a ward with its initial beds.")
  ResponseEntity<ApiResponse<WardResponse>> createWard(@Valid @RequestBody CreateWardRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
        "Ward created successfully", service.createWard(tenantContext.hospitalId(), request)));
  }

  @GetMapping("/wards/{wardId}/beds")
  @Operation(summary = "List beds in a ward")
  ApiResponse<List<BedResponse>> beds(@PathVariable Long wardId) {
    return ApiResponse.success("Beds retrieved successfully",
        service.beds(tenantContext.hospitalId(), wardId));
  }

  @PostMapping("/wards/{wardId}/beds/add")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Add beds", description = "Admin-only: grows the ward by a number of beds.")
  ApiResponse<WardResponse> add(@PathVariable Long wardId, @Valid @RequestBody BedCountRequest request) {
    return ApiResponse.success("Beds added successfully",
        service.addBeds(tenantContext.hospitalId(), wardId, request));
  }

  @PostMapping("/wards/{wardId}/beds/reduce")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Reduce beds",
      description = "Admin-only: removes free beds, highest number first. Occupied beds are never removed.")
  ApiResponse<WardResponse> reduce(@PathVariable Long wardId, @Valid @RequestBody BedCountRequest request) {
    return ApiResponse.success("Beds removed successfully",
        service.reduceBeds(tenantContext.hospitalId(), wardId, request));
  }

  @PatchMapping("/{bedId}/assign")
  @PreAuthorize(WARD_STAFF)
  @Operation(summary = "Assign a patient to a bed")
  ApiResponse<BedResponse> assign(@PathVariable Long bedId, @Valid @RequestBody AssignBedRequest request) {
    return ApiResponse.success("Bed assigned successfully",
        service.assign(tenantContext.hospitalId(), bedId, request));
  }

  @PatchMapping("/{bedId}/release")
  @PreAuthorize(WARD_STAFF)
  @Operation(summary = "Release an occupied bed")
  ApiResponse<BedResponse> release(@PathVariable Long bedId) {
    return ApiResponse.success("Bed released successfully",
        service.release(tenantContext.hospitalId(), bedId));
  }

  @PatchMapping("/{bedId}/maintenance")
  @PreAuthorize(WARD_STAFF)
  @Operation(summary = "Put a bed into or out of maintenance")
  ApiResponse<BedResponse> maintenance(@PathVariable Long bedId,
      @Valid @RequestBody MaintenanceRequest request) {
    return ApiResponse.success("Bed updated successfully",
        service.setMaintenance(tenantContext.hospitalId(), bedId, request.underMaintenance()));
  }
}
