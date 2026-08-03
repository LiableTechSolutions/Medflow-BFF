package com.medflow.modules.audit.controller;

import com.medflow.modules.audit.api.AuditService;
import com.medflow.modules.audit.api.response.AuditLogResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
class AuditController {

  private final AuditService service;
  private final TenantContext tenantContext;

  AuditController(AuditService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "List audit entries",
      description = "Admin-only: the workspace trail, newest first, filterable by entity type and user.")
  ApiResponse<PageResponse<AuditLogResponse>> search(
      @RequestParam(required = false) String entityType,
      @RequestParam(required = false) Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Audit entries retrieved successfully",
        service.search(tenantContext.hospitalId(), entityType, userId, page, size));
  }
}
