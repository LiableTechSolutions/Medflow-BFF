package com.medflow.modules.appointments.controller;

import com.medflow.modules.appointments.api.AppointmentService;
import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.modules.appointments.api.request.BookAppointmentRequest;
import com.medflow.modules.appointments.api.request.RescheduleAppointmentRequest;
import com.medflow.modules.appointments.api.response.AppointmentResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/v1/appointments")
class AppointmentController {

  private final AppointmentService service;
  private final TenantContext tenantContext;

  AppointmentController(AppointmentService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @PostMapping
  @Operation(summary = "Book appointment",
      description = "Schedules a visit, assigns the day's queue number and flags overlapping slots.")
  ResponseEntity<ApiResponse<AppointmentResponse>> book(
      @Valid @RequestBody BookAppointmentRequest request) {
    var user = tenantContext.require();
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
        "Appointment booked successfully",
        service.book(user.hospitalId(), user.userId(), request)));
  }

  @GetMapping
  @Operation(summary = "List appointments",
      description = "Filters by status, doctor, patient and calendar day (UTC).")
  ApiResponse<PageResponse<AppointmentResponse>> search(
      @RequestParam(required = false) AppointmentStatus status,
      @RequestParam(required = false) Long doctorId,
      @RequestParam(required = false) Long patientId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Appointments retrieved successfully",
        service.search(tenantContext.hospitalId(), status, doctorId, patientId, date, page, size));
  }

  @GetMapping("/{appointmentId}")
  @Operation(summary = "Get appointment", description = "Returns an appointment by identifier.")
  ApiResponse<AppointmentResponse> find(@PathVariable Long appointmentId) {
    return ApiResponse.success("Appointment retrieved successfully",
        service.findById(tenantContext.hospitalId(), appointmentId));
  }

  @PatchMapping("/{appointmentId}/confirm")
  @Operation(summary = "Confirm appointment", description = "Booked → confirmed.")
  ApiResponse<AppointmentResponse> confirm(@PathVariable Long appointmentId) {
    return transition(appointmentId, AppointmentStatus.CONFIRMED, "Appointment confirmed successfully");
  }

  @PatchMapping("/{appointmentId}/check-in")
  @Operation(summary = "Check in patient", description = "Marks the patient as arrived and waiting.")
  ApiResponse<AppointmentResponse> checkIn(@PathVariable Long appointmentId) {
    return transition(appointmentId, AppointmentStatus.CHECKED_IN, "Patient checked in successfully");
  }

  @PatchMapping("/{appointmentId}/start-consultation")
  @Operation(summary = "Start consultation", description = "The doctor has called the patient in.")
  ApiResponse<AppointmentResponse> startConsultation(@PathVariable Long appointmentId) {
    return transition(appointmentId, AppointmentStatus.IN_CONSULTATION, "Consultation started");
  }

  @PatchMapping("/{appointmentId}/complete")
  @Operation(summary = "Complete appointment",
      description = "Closes the visit; the fee counts toward revenue.")
  ApiResponse<AppointmentResponse> complete(@PathVariable Long appointmentId) {
    return transition(appointmentId, AppointmentStatus.COMPLETED, "Appointment completed successfully");
  }

  @PatchMapping("/{appointmentId}/cancel")
  @Operation(summary = "Cancel appointment", description = "Cancels an appointment that has not closed.")
  ApiResponse<AppointmentResponse> cancel(@PathVariable Long appointmentId) {
    return transition(appointmentId, AppointmentStatus.CANCELLED, "Appointment cancelled successfully");
  }

  @PatchMapping("/{appointmentId}/no-show")
  @Operation(summary = "Mark no-show", description = "The patient did not arrive for the slot.")
  ApiResponse<AppointmentResponse> noShow(@PathVariable Long appointmentId) {
    return transition(appointmentId, AppointmentStatus.NO_SHOW, "Appointment marked as no-show");
  }

  @PatchMapping("/{appointmentId}/reschedule")
  @Operation(summary = "Reschedule appointment",
      description = "Moves the slot and resets the status to booked.")
  ApiResponse<AppointmentResponse> reschedule(@PathVariable Long appointmentId,
      @Valid @RequestBody RescheduleAppointmentRequest request) {
    return ApiResponse.success("Appointment rescheduled successfully",
        service.reschedule(tenantContext.hospitalId(), appointmentId, request));
  }

  private ApiResponse<AppointmentResponse> transition(Long appointmentId, AppointmentStatus target,
      String message) {
    return ApiResponse.success(message,
        service.transition(tenantContext.hospitalId(), appointmentId, target));
  }
}
