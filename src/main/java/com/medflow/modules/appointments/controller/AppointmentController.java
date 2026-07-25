package com.medflow.modules.appointments.controller;

import com.medflow.modules.appointments.api.AppointmentService;
import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.modules.appointments.api.request.BookAppointmentRequest;
import com.medflow.modules.appointments.api.request.RescheduleAppointmentRequest;
import com.medflow.modules.appointments.api.response.AppointmentResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
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

  AppointmentController(AppointmentService service) {
    this.service = service;
  }

  @PostMapping
  @Operation(summary = "Book appointment", description = "Schedules a visit; overlapping slots are flagged via notifications.")
  ResponseEntity<ApiResponse<AppointmentResponse>> book(
      @Valid @RequestBody BookAppointmentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Appointment booked successfully", service.book(request)));
  }

  @GetMapping
  @Operation(summary = "List appointments", description = "Filters by status, doctor, patient and calendar day (UTC).")
  ApiResponse<PageResponse<AppointmentResponse>> search(
      @RequestParam(required = false) AppointmentStatus status,
      @RequestParam(required = false) UUID doctorId,
      @RequestParam(required = false) UUID patientId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Appointments retrieved successfully",
        service.search(status, doctorId, patientId, date, page, size));
  }

  @GetMapping("/{appointmentId}")
  @Operation(summary = "Get appointment", description = "Returns an appointment by identifier.")
  ApiResponse<AppointmentResponse> find(@PathVariable UUID appointmentId) {
    return ApiResponse.success("Appointment retrieved successfully",
        service.findById(appointmentId));
  }

  @PatchMapping("/{appointmentId}/confirm")
  @Operation(summary = "Confirm appointment", description = "PENDING → CONFIRMED.")
  ApiResponse<AppointmentResponse> confirm(@PathVariable UUID appointmentId) {
    return ApiResponse.success("Appointment confirmed successfully",
        service.confirm(appointmentId));
  }

  @PatchMapping("/{appointmentId}/complete")
  @Operation(summary = "Complete appointment", description = "CONFIRMED → COMPLETED; the fee counts toward revenue.")
  ApiResponse<AppointmentResponse> complete(@PathVariable UUID appointmentId) {
    return ApiResponse.success("Appointment completed successfully",
        service.complete(appointmentId));
  }

  @PatchMapping("/{appointmentId}/cancel")
  @Operation(summary = "Cancel appointment", description = "Cancels a pending or confirmed appointment.")
  ApiResponse<AppointmentResponse> cancel(@PathVariable UUID appointmentId) {
    return ApiResponse.success("Appointment cancelled successfully",
        service.cancel(appointmentId));
  }

  @PatchMapping("/{appointmentId}/reschedule")
  @Operation(summary = "Reschedule appointment", description = "Moves the slot and resets the status to PENDING.")
  ApiResponse<AppointmentResponse> reschedule(@PathVariable UUID appointmentId,
      @Valid @RequestBody RescheduleAppointmentRequest request) {
    return ApiResponse.success("Appointment rescheduled successfully",
        service.reschedule(appointmentId, request));
  }
}
