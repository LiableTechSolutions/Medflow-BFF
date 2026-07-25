package com.medflow.modules.appointments.api;

import com.medflow.modules.appointments.api.request.BookAppointmentRequest;
import com.medflow.modules.appointments.api.request.RescheduleAppointmentRequest;
import com.medflow.modules.appointments.api.response.AppointmentResponse;
import com.medflow.shared.api.PageResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Public API of the Appointments module. */
public interface AppointmentService {

  AppointmentResponse book(BookAppointmentRequest request);

  AppointmentResponse findById(UUID appointmentId);

  PageResponse<AppointmentResponse> search(AppointmentStatus status, UUID doctorId,
      UUID patientId, LocalDate date, int page, int size);

  AppointmentResponse confirm(UUID appointmentId);

  AppointmentResponse complete(UUID appointmentId);

  AppointmentResponse cancel(UUID appointmentId);

  /** Moves the appointment and resets it to PENDING for re-confirmation. */
  AppointmentResponse reschedule(UUID appointmentId, RescheduleAppointmentRequest request);

  // --- Aggregates consumed by the analytics module ---

  long countOnDate(LocalDate date);

  long countActive();

  BigDecimal completedRevenueBetween(Instant from, Instant to);

  List<DailyAppointmentCount> dailyCounts(LocalDate from, LocalDate to);
}
