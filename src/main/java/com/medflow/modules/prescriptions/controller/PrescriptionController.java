package com.medflow.modules.prescriptions.controller;

import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.prescriptions.api.PrescriptionService;
import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.modules.prescriptions.api.request.CreatePrescriptionRequest;
import com.medflow.modules.prescriptions.api.response.PrescriptionResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prescriptions")
class PrescriptionController {

  private final PrescriptionService service;
  private final DoctorService doctorService;
  private final TenantContext tenantContext;

  PrescriptionController(PrescriptionService service, DoctorService doctorService,
      TenantContext tenantContext) {
    this.service = service;
    this.doctorService = doctorService;
    this.tenantContext = tenantContext;
  }

  @PostMapping
  @PreAuthorize("hasRole('DOCTOR')")
  @Operation(summary = "Issue prescription",
      description = "Writes a diagnosis with one or more medication lines and signs it. "
          + "The doctor must be writing for themselves.")
  ResponseEntity<ApiResponse<PrescriptionResponse>> create(
      @Valid @RequestBody CreatePrescriptionRequest request) {
    requireSelf(request.doctorId());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
        "Prescription created successfully",
        service.create(tenantContext.hospitalId(), request)));
  }

  /**
   * A doctor can only ever write a prescription as themselves — never trust a client-supplied
   * doctorId on its own, even though the field is required for the request shape.
   */
  private void requireSelf(Long requestedDoctorId) {
    var self = doctorService.findByCurrentUser(tenantContext.hospitalId(), tenantContext.userId());
    if (!self.id().equals(requestedDoctorId)) {
      throw new AccessDeniedException("A doctor can only write prescriptions as themselves");
    }
  }

  @GetMapping
  @Operation(summary = "List prescriptions",
      description = "Filters by patient (history), doctor, status and date. `query` free-text "
          + "matches diagnosis, patient name and doctor name.")
  ApiResponse<PageResponse<PrescriptionResponse>> search(
      @RequestParam(required = false) Long patientId,
      @RequestParam(required = false) Long doctorId,
      @RequestParam(required = false) PrescriptionStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issuedOn,
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Prescriptions retrieved successfully", service.search(
        tenantContext.hospitalId(), patientId, doctorId, status, issuedOn, query, page, size));
  }

  @GetMapping("/{prescriptionId}")
  @Operation(summary = "Get prescription", description = "Returns a prescription with its medicines.")
  ApiResponse<PrescriptionResponse> find(@PathVariable Long prescriptionId) {
    return ApiResponse.success("Prescription retrieved successfully",
        service.findById(tenantContext.hospitalId(), prescriptionId));
  }

  @PostMapping("/{prescriptionId}/send")
  @Operation(summary = "Send prescription to patient",
      description = "Emails/WhatsApps/SMSes the prescription to the patient on file. "
          + "Viewing or printing never does this on its own — only this explicit action does.")
  ApiResponse<Void> send(@PathVariable Long prescriptionId) {
    service.send(tenantContext.hospitalId(), prescriptionId);
    return ApiResponse.success("Prescription sent to the patient", null);
  }

  @PatchMapping("/{prescriptionId}/complete")
  @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
  @Operation(summary = "Complete prescription",
      description = "Marks the course as finished. The authoring doctor or an admin only.")
  ApiResponse<PrescriptionResponse> complete(@PathVariable Long prescriptionId) {
    requireOwnerOrAdmin(prescriptionId);
    return ApiResponse.success("Prescription completed successfully",
        service.complete(tenantContext.hospitalId(), prescriptionId));
  }

  @PatchMapping("/{prescriptionId}/cancel")
  @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
  @Operation(summary = "Cancel prescription",
      description = "Withdraws an active prescription. The authoring doctor or an admin only.")
  ApiResponse<PrescriptionResponse> cancel(@PathVariable Long prescriptionId) {
    requireOwnerOrAdmin(prescriptionId);
    return ApiResponse.success("Prescription cancelled successfully",
        service.cancel(tenantContext.hospitalId(), prescriptionId));
  }

  private void requireOwnerOrAdmin(Long prescriptionId) {
    var caller = tenantContext.require();
    if ("ADMIN".equals(caller.roleCode())) {
      return;
    }
    var prescription = service.findById(tenantContext.hospitalId(), prescriptionId);
    var self = doctorService.findByCurrentUser(tenantContext.hospitalId(), caller.userId());
    if (!self.id().equals(prescription.doctorId())) {
      throw new AccessDeniedException("Only the authoring doctor or an admin can do this");
    }
  }
}
