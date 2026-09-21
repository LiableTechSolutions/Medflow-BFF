package com.medflow.modules.appointments.api;

import com.medflow.modules.appointments.api.request.BookAppointmentRequest;
import com.medflow.modules.appointments.api.request.RescheduleAppointmentRequest;
import com.medflow.modules.appointments.api.response.AppointmentResponse;
import com.medflow.modules.appointments.api.response.AvailableSlotsResponse;
import com.medflow.modules.appointments.api.response.PublicQueueBoardResponse;
import com.medflow.modules.appointments.api.response.QueueStatusResponse;
import com.medflow.shared.api.PageResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Public API of the Appointments module. */
public interface AppointmentService {

  AppointmentResponse book(Long hospitalId, Long bookedByUserId, BookAppointmentRequest request);

  AppointmentResponse findById(Long hospitalId, Long appointmentId);

  /** Where this appointment stands in its doctor's live queue for the day. */
  QueueStatusResponse queueStatus(Long hospitalId, Long appointmentId);

  /** Open consulting slots for the doctor on this date, per their configured availability. */
  AvailableSlotsResponse availableSlots(Long hospitalId, Long doctorId, LocalDate date);

  /**
   * Unauthenticated read-only queue board for a waiting-room TV or a link shared with
   * patients. {@code hospitalCode} and {@code doctorId} are both required and cross-checked
   * against each other (no anonymous access to hospital-wide or cross-tenant data).
   */
  PublicQueueBoardResponse publicQueueBoard(String hospitalCode, Long doctorId, LocalDate date);

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
