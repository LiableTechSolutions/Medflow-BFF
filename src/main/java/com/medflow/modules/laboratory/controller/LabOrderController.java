package com.medflow.modules.laboratory.controller;

import com.medflow.modules.laboratory.api.LabOrderService;
import com.medflow.modules.laboratory.api.LabOrderStatus;
import com.medflow.modules.laboratory.api.LabPriority;
import com.medflow.modules.laboratory.api.request.CompleteLabOrderRequest;
import com.medflow.modules.laboratory.api.request.CreateLabOrderRequest;
import com.medflow.modules.laboratory.api.response.LabOrderResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lab-orders")
class LabOrderController {

  private final LabOrderService service;
  private final TenantContext tenantContext;

  LabOrderController(LabOrderService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @PostMapping
  @Operation(summary = "Create lab order", description = "Routes a test order into the specimen queue.")
  ResponseEntity<ApiResponse<LabOrderResponse>> create(
      @Valid @RequestBody CreateLabOrderRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
        "Lab order created successfully", service.create(tenantContext.hospitalId(), request)));
  }

  @GetMapping
  @Operation(summary = "List lab orders",
      description = "Filters the queue by status, priority and patient.")
  ApiResponse<PageResponse<LabOrderResponse>> search(
      @RequestParam(required = false) LabOrderStatus status,
      @RequestParam(required = false) LabPriority priority,
      @RequestParam(required = false) Long patientId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Lab orders retrieved successfully",
        service.search(tenantContext.hospitalId(), status, priority, patientId, page, size));
  }

  @GetMapping("/{labOrderId}")
  @Operation(summary = "Get lab order", description = "Returns a lab order by identifier.")
  ApiResponse<LabOrderResponse> find(@PathVariable Long labOrderId) {
    return ApiResponse.success("Lab order retrieved successfully",
        service.findById(tenantContext.hospitalId(), labOrderId));
  }

  @PatchMapping("/{labOrderId}/start")
  @Operation(summary = "Start processing", description = "ORDERED → IN_PROGRESS.")
  ApiResponse<LabOrderResponse> start(@PathVariable Long labOrderId) {
    return ApiResponse.success("Lab order moved to processing",
        service.startProcessing(tenantContext.hospitalId(), labOrderId));
  }

  @PatchMapping("/{labOrderId}/complete")
  @Operation(summary = "Record results",
      description = "Signs off results and notifies the workspace.")
  ApiResponse<LabOrderResponse> complete(@PathVariable Long labOrderId,
      @Valid @RequestBody CompleteLabOrderRequest request) {
    return ApiResponse.success("Lab order completed successfully",
        service.complete(tenantContext.hospitalId(), labOrderId, request));
  }

  @PatchMapping("/{labOrderId}/cancel")
  @Operation(summary = "Cancel lab order",
      description = "Cancels an order that has not been completed.")
  ApiResponse<LabOrderResponse> cancel(@PathVariable Long labOrderId) {
    return ApiResponse.success("Lab order cancelled successfully",
        service.cancel(tenantContext.hospitalId(), labOrderId));
  }
}
