package com.medflow.modules.doctors.controller;

import com.medflow.modules.doctors.api.DoctorAvailability;
import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.doctors.api.request.ChangeAvailabilityRequest;
import com.medflow.modules.doctors.api.request.CreateDoctorRequest;
import com.medflow.modules.doctors.api.request.UpdateDoctorRequest;
import com.medflow.modules.doctors.api.response.DoctorResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

  DoctorController(DoctorService service) {
    this.service = service;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Onboard doctor", description = "Admin-only: registers a doctor with credentials and specialty.")
  ResponseEntity<ApiResponse<DoctorResponse>> create(@Valid @RequestBody CreateDoctorRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Doctor created successfully", service.create(request)));
  }

  @GetMapping
  @Operation(summary = "List doctors", description = "Filters the roster by name, specialty and availability.")
  ApiResponse<PageResponse<DoctorResponse>> search(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) String specialty,
      @RequestParam(required = false) DoctorAvailability availability,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Doctors retrieved successfully",
        service.search(query, specialty, availability, page, size));
  }

  @GetMapping("/{doctorId}")
  @Operation(summary = "Get doctor", description = "Returns a doctor's profile by identifier.")
  ApiResponse<DoctorResponse> find(@PathVariable UUID doctorId) {
    return ApiResponse.success("Doctor retrieved successfully", service.findById(doctorId));
  }

  @PutMapping("/{doctorId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update doctor", description = "Admin-only: updates profile, department and fee.")
  ApiResponse<DoctorResponse> update(@PathVariable UUID doctorId,
      @Valid @RequestBody UpdateDoctorRequest request) {
    return ApiResponse.success("Doctor updated successfully", service.update(doctorId, request));
  }

  @PatchMapping("/{doctorId}/availability")
  @Operation(summary = "Change availability", description = "Marks a doctor available, on duty or on leave.")
  ApiResponse<DoctorResponse> changeAvailability(@PathVariable UUID doctorId,
      @Valid @RequestBody ChangeAvailabilityRequest request) {
    return ApiResponse.success("Doctor availability updated successfully",
        service.changeAvailability(doctorId, request.availability()));
  }
}
