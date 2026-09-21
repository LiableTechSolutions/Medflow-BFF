package com.medflow.modules.appointments.controller;

import com.medflow.modules.appointments.api.AppointmentService;
import com.medflow.modules.appointments.api.response.PublicQueueBoardResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.security.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unauthenticated, read-only queue board - meant for a waiting-room TV or a link shared
 * with patients in a booking notification. Deliberately its own controller, separate
 * from {@link AppointmentController}: every other endpoint there trusts
 * {@code TenantContext}, which an anonymous caller doesn't have.
 */
@RestController
@RequestMapping("/api/v1/public/queue")
class PublicQueueController {

  private final AppointmentService service;

  PublicQueueController(AppointmentService service) {
    this.service = service;
  }

  @GetMapping
  @PublicApi
  @Operation(summary = "Get public queue board",
      description = "Read-only queue board for one doctor's day. No authentication - "
          + "hospitalCode and doctorId are cross-checked against each other.")
  ApiResponse<PublicQueueBoardResponse> board(
      @RequestParam String hospitalCode,
      @RequestParam Long doctorId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ApiResponse.success("Queue board retrieved successfully",
        service.publicQueueBoard(hospitalCode, doctorId, date));
  }
}
