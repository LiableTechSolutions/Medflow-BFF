package com.medflow.modules.patients.controller;

import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.request.CreatePatientRequest;
import com.medflow.modules.patients.api.response.PatientResponse;
import com.medflow.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
class PatientController {
  private final PatientService service;
  PatientController(PatientService service) { this.service = service; }
  @PostMapping @Operation(summary = "Create patient", description = "Registers a patient in the active tenant context.")
  ResponseEntity<ApiResponse<PatientResponse>> create(@Valid @RequestBody CreatePatientRequest request, HttpServletRequest http) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Patient created successfully", service.create(request), http.getHeader("X-Trace-Id")));
  }
  @GetMapping("/{patientId}") @Operation(summary = "Get patient", description = "Returns a patient by identifier.")
  ApiResponse<PatientResponse> find(@PathVariable UUID patientId, HttpServletRequest http) {
    return ApiResponse.success("Patient retrieved successfully", service.findById(patientId), http.getHeader("X-Trace-Id"));
  }
}
