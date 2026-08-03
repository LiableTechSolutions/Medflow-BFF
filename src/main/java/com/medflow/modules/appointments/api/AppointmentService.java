package com.medflow.modules.appointments.api;

import com.medflow.modules.appointments.api.request.BookAppointmentRequest;
import com.medflow.modules.appointments.api.request.RescheduleAppointmentRequest;
import com.medflow.modules.appointments.api.response.AppointmentResponse;
import com.medflow.shared.api.PageResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Public API of the Appointments module. */
public interface AppointmentService {

  AppointmentResponse book(Long hospitalId, Long bookedByUserId, BookAppointmentRequest request);

  AppointmentResponse findById(Long hospitalId, Long appointmentId);

  PageResponse<AppointmentResponse> search(Long hospitalId, AppointmentStatus status,
      Long doctorId, Long patientId, LocalDate date, int page, int size);

  /** Moves the appointment through its workflow; illegal transitions are rejected. */
  AppointmentResponse transition(Long hospitalId, Long appointmentId, AppointmentStatus target);

  AppointmentResponse reschedule(Long hospitalId, Long appointmentId,
      RescheduleAppointmentRequest request);

  // --- Aggregates consumed by the analytics module ---

  long countOnDate(Long hospitalId, LocalDate date);

  long countActive(Long hospitalId);

  BigDecimal completedRevenueBetween(Long hospitalId, Instant from, Instant to);

  List<DailyAppointmentCount> dailyCounts(Long hospitalId, LocalDate from, LocalDate to);
}
