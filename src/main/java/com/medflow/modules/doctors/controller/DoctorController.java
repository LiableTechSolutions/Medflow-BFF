package com.medflow.modules.doctors.controller;

import com.medflow.modules.audit.api.AuditService;
import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.doctors.api.request.AddAvailabilityRequest;
import com.medflow.modules.doctors.api.request.AssignStaffRequest;
import com.medflow.modules.doctors.api.request.ChangeDoctorStatusRequest;
import com.medflow.modules.doctors.api.request.CreateDoctorRequest;
import com.medflow.modules.doctors.api.request.UpdateDoctorRequest;
import com.medflow.modules.doctors.api.response.DoctorAvailabilityResponse;
import com.medflow.modules.doctors.api.response.DoctorResponse;
import com.medflow.modules.doctors.api.response.DoctorStaffResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.domain.AccountStatus;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/doctors")
class DoctorController {

  private final DoctorService service;
  private final TenantContext tenantContext;
  private final AuditService auditService;

  DoctorController(DoctorService service, TenantContext tenantContext, AuditService auditService) {
    this.service = service;
    this.tenantContext = tenantContext;
    this.auditService = auditService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Onboard doctor",
      description = "Admin-only: creates the doctor's account and clinical profile.")
  ResponseEntity<ApiResponse<DoctorResponse>> create(@Valid @RequestBody CreateDoctorRequest request) {
    var created = service.create(tenantContext.hospitalId(), request);
    auditService.record("DOCTOR_CREATED", "doctor", created.id(),
        "{\"doctorCode\":\"%s\"}".formatted(created.doctorCode()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Doctor created successfully", created));
  }

  @GetMapping
  @Operation(summary = "List doctors",
      description = "Filters the roster by name, code, specialty and status.")
  ApiResponse<PageResponse<DoctorResponse>> search(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) String specialty,
      @RequestParam(required = false) AccountStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Doctors retrieved successfully",
        service.search(tenantContext.hospitalId(), query, specialty, status, page, size));
  }

  @GetMapping("/{doctorId}")
  @Operation(summary = "Get doctor", description = "Returns a doctor's profile by identifier.")
  ApiResponse<DoctorResponse> find(@PathVariable Long doctorId) {
    return ApiResponse.success("Doctor retrieved successfully",
        service.findById(tenantContext.hospitalId(), doctorId));
  }

  @PutMapping("/{doctorId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update doctor",
      description = "Admin-only: updates profile, credentials and consulting fee.")
  ApiResponse<DoctorResponse> update(@PathVariable Long doctorId,
      @Valid @RequestBody UpdateDoctorRequest request) {
    var updated = service.update(tenantContext.hospitalId(), doctorId, request);
    auditService.record("DOCTOR_UPDATED", "doctor", doctorId, null);
    return ApiResponse.success("Doctor updated successfully", updated);
  }

  @PatchMapping("/{doctorId}/status")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Change doctor status",
      description = "Admin-only: activates, deactivates or suspends a doctor and their sign-in.")
  ApiResponse<DoctorResponse> changeStatus(@PathVariable Long doctorId,
      @Valid @RequestBody ChangeDoctorStatusRequest request) {
    var updated = service.changeStatus(tenantContext.hospitalId(), doctorId, request.status());
    auditService.record("DOCTOR_STATUS_CHANGED", "doctor", doctorId,
        "{\"status\":\"%s\"}".formatted(request.status()));
    return ApiResponse.success("Doctor status updated successfully", updated);
  }

  @GetMapping("/{doctorId}/staff")
  @Operation(summary = "List assigned staff",
      description = "Accounts attached to this doctor (front desk, nurses, assistants).")
  ApiResponse<List<DoctorStaffResponse>> staff(@PathVariable Long doctorId) {
    return ApiResponse.success("Assigned staff retrieved successfully",
        service.staff(tenantContext.hospitalId(), doctorId));
  }

  @PostMapping("/{doctorId}/staff")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Assign staff", description = "Admin-only: attaches a staff account to the doctor.")
  ResponseEntity<ApiResponse<DoctorStaffResponse>> assignStaff(@PathVariable Long doctorId,
      @Valid @RequestBody AssignStaffRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
        "Staff assigned successfully",
        service.assignStaff(tenantContext.hospitalId(), doctorId, request)));
  }

  @GetMapping("/{doctorId}/availability")
  @Operation(summary = "List availability",
      description = "Consulting windows: recurring weekdays and one-off date overrides.")
  ApiResponse<List<DoctorAvailabilityResponse>> availability(@PathVariable Long doctorId) {
    return ApiResponse.success("Availability retrieved successfully",
        service.availability(tenantContext.hospitalId(), doctorId));
  }

  @PostMapping("/{doctorId}/availability")
  @Operation(summary = "Add availability", description = "Adds a consulting window for the doctor.")
  ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> addAvailability(
      @PathVariable Long doctorId, @Valid @RequestBody AddAvailabilityRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
        "Availability added successfully",
        service.addAvailability(tenantContext.hospitalId(), doctorId, request)));
  }

  @DeleteMapping("/{doctorId}/availability/{availabilityId}")
  @Operation(summary = "Remove availability", description = "Deletes one consulting window.")
  ApiResponse<Void> removeAvailability(@PathVariable Long doctorId,
      @PathVariable Long availabilityId) {
    service.removeAvailability(tenantContext.hospitalId(), doctorId, availabilityId);
    return ApiResponse.success("Availability removed successfully", null);
  }
}
