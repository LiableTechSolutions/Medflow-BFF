package com.medflow.modules.patients.controller;

import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.request.CreatePatientRequest;
import com.medflow.modules.patients.api.request.UpdatePatientRequest;
import com.medflow.modules.patients.api.response.PatientResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
class PatientController {

  private final PatientService service;

  PatientController(PatientService service) {
    this.service = service;
  }

  @PostMapping
  @Operation(summary = "Create patient", description = "Registers a patient with intake details.")
  ResponseEntity<ApiResponse<PatientResponse>> create(
      @Valid @RequestBody CreatePatientRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Patient created successfully", service.create(request)));
  }

  @GetMapping
  @Operation(summary = "List patients", description = "Searches the patient roster by name or email.")
  ApiResponse<PageResponse<PatientResponse>> search(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Patients retrieved successfully",
        service.search(query, page, size));
  }

  @GetMapping("/{patientId}")
  @Operation(summary = "Get patient", description = "Returns a patient by identifier.")
  ApiResponse<PatientResponse> find(@PathVariable UUID patientId) {
    return ApiResponse.success("Patient retrieved successfully", service.findById(patientId));
  }

  @PutMapping("/{patientId}")
  @Operation(summary = "Update patient", description = "Replaces the patient's profile, including archive status.")
  ApiResponse<PatientResponse> update(@PathVariable UUID patientId,
      @Valid @RequestBody UpdatePatientRequest request) {
    return ApiResponse.success("Patient updated successfully",
        service.update(patientId, request));
  }
}
