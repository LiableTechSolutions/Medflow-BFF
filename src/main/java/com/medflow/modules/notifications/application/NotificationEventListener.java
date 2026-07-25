package com.medflow.modules.notifications.application;

import com.medflow.modules.appointments.api.AppointmentBookedEvent;
import com.medflow.modules.appointments.api.AppointmentScheduleConflictEvent;
import com.medflow.modules.laboratory.api.LabOrderCompletedEvent;
import com.medflow.modules.notifications.api.NotificationCategory;
import com.medflow.modules.notifications.api.NotificationSeverity;
import com.medflow.modules.notifications.domain.entity.Notification;
import com.medflow.modules.notifications.domain.repository.NotificationRepository;
import com.medflow.modules.pharmacy.api.MedicationLowStockEvent;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Turns domain events from other modules into entries in the workspace alert feed. */
@Component
class NotificationEventListener {

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("MMM d, HH:mm 'UTC'").withZone(ZoneOffset.UTC);

  private final NotificationRepository repository;

  NotificationEventListener(NotificationRepository repository) {
    this.repository = repository;
  }

  @ApplicationModuleListener
  void on(AppointmentBookedEvent event) {
    repository.save(new Notification(NotificationCategory.APPOINTMENT, NotificationSeverity.INFO,
        "Appointment booked",
        event.patientName() + " is scheduled with " + event.doctorName() + " at "
            + TIME_FORMAT.format(event.scheduledAt()) + "."));
  }

  @ApplicationModuleListener
  void on(AppointmentScheduleConflictEvent event) {
    repository.save(new Notification(NotificationCategory.APPOINTMENT,
        NotificationSeverity.WARNING, "Schedule conflict",
        event.doctorName() + " has overlapping bookings at "
            + TIME_FORMAT.format(event.scheduledAt()) + "."));
  }

  @ApplicationModuleListener
  void on(LabOrderCompletedEvent event) {
    repository.save(new Notification(NotificationCategory.LABORATORY, NotificationSeverity.INFO,
        "Lab results ready",
        event.patientName() + "'s " + event.testName() + " results just came in."));
  }

  @ApplicationModuleListener
  void on(MedicationLowStockEvent event) {
    repository.save(new Notification(NotificationCategory.PHARMACY, NotificationSeverity.WARNING,
        "Low stock alert",
        "Pharmacy flagged " + event.name() + " running low ("
            + event.stockQuantity() + " left, reorder at " + event.reorderLevel() + ")."));
  }
}
