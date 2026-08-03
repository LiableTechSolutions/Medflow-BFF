package com.medflow.modules.pharmacy.controller;

import com.medflow.modules.pharmacy.api.MedicationService;
import com.medflow.modules.pharmacy.api.request.AdjustStockRequest;
import com.medflow.modules.pharmacy.api.request.CreateMedicationRequest;
import com.medflow.modules.pharmacy.api.request.UpdateMedicationRequest;
import com.medflow.modules.pharmacy.api.response.MedicationResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pharmacy/medications")
class MedicationController {

  private final MedicationService service;
  private final TenantContext tenantContext;

  MedicationController(MedicationService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @PostMapping
  @Operation(summary = "Add medication",
      description = "Registers a medication in the pharmacy catalogue.")
  ResponseEntity<ApiResponse<MedicationResponse>> create(
      @Valid @RequestBody CreateMedicationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
        "Medication created successfully", service.create(tenantContext.hospitalId(), request)));
  }

  @GetMapping
  @Operation(summary = "List medications",
      description = "Searches the catalogue; lowStockOnly returns items at or below reorder level.")
  ApiResponse<PageResponse<MedicationResponse>> search(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "false") boolean lowStockOnly,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Medications retrieved successfully",
        service.search(tenantContext.hospitalId(), query, lowStockOnly, page, size));
  }

  @GetMapping("/{medicationId}")
  @Operation(summary = "Get medication", description = "Returns a medication by identifier.")
  ApiResponse<MedicationResponse> find(@PathVariable Long medicationId) {
    return ApiResponse.success("Medication retrieved successfully",
        service.findById(tenantContext.hospitalId(), medicationId));
  }

  @PutMapping("/{medicationId}")
  @Operation(summary = "Update medication",
      description = "Updates catalogue details and the reorder level.")
  ApiResponse<MedicationResponse> update(@PathVariable Long medicationId,
      @Valid @RequestBody UpdateMedicationRequest request) {
    return ApiResponse.success("Medication updated successfully",
        service.update(tenantContext.hospitalId(), medicationId, request));
  }

  @PatchMapping("/{medicationId}/stock")
  @Operation(summary = "Adjust stock",
      description = "Restocks (+) or dispenses (−); crossing the reorder level raises an alert.")
  ApiResponse<MedicationResponse> adjustStock(@PathVariable Long medicationId,
      @Valid @RequestBody AdjustStockRequest request) {
    return ApiResponse.success("Medication stock adjusted successfully",
        service.adjustStock(tenantContext.hospitalId(), medicationId, request));
  }
}
