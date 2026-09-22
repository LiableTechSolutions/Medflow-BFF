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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Turns domain events from other modules into entries in the workspace alert feed.
 * Listeners run after commit, in their own transaction and outside any request, so every
 * event carries its own tenant rather than reading one from a security context.
 */
@Component
@EnableConfigurationProperties(NotificationProperties.class)
class NotificationEventListener {

  private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("MMM d, HH:mm 'UTC'").withZone(ZoneOffset.UTC);

  private final NotificationRepository repository;
  private final NotificationProperties properties;

  NotificationEventListener(NotificationRepository repository, NotificationProperties properties) {
    this.repository = repository;
    this.properties = properties;
  }

  /**
   * No WhatsApp/SMS provider is wired up yet (needs a real account + API credentials —
   * see the "Appointment Queue, Notifications & Data Fixes" doc's open questions). Until
   * then, this logs what would have been sent and still raises the in-app notification
   * with the same message, including a link to the live queue, so the flow is visible
   * end-to-end without a real external send.
   *
   * <p>The link carries a pre-signed token (minted by AppointmentServiceImpl, which has
   * the tenant context this listener doesn't) rather than raw hospital/doctor ids —
   * unguessable and self-expiring, since the patient receiving this has no staff login
   * to gate access some other way.
   */
  @ApplicationModuleListener
  void on(AppointmentBookedEvent event) {
    var queueLink = properties.frontendBaseUrl() + "/public/queue?token=" + event.queueLinkToken();
    var message = event.patientName() + " is scheduled with " + event.doctorName() + " at "
        + TIME_FORMAT.format(event.scheduledAt()) + ". Live queue: " + queueLink;
    repository.save(new Notification(event.hospitalId(), NotificationCategory.APPOINTMENT,
        NotificationSeverity.INFO, "Appointment booked", message));
    log.info("[whatsapp-mock] would notify {} ({}): {}", event.patientName(),
        event.patientPhone(), message);
  }

  @ApplicationModuleListener
  void on(AppointmentScheduleConflictEvent event) {
    repository.save(new Notification(event.hospitalId(), NotificationCategory.APPOINTMENT,
        NotificationSeverity.WARNING, "Schedule conflict",
        event.doctorName() + " has overlapping bookings at "
            + TIME_FORMAT.format(event.scheduledAt()) + "."));
  }

  @ApplicationModuleListener
  void on(LabOrderCompletedEvent event) {
    repository.save(new Notification(event.hospitalId(), NotificationCategory.LABORATORY,
        NotificationSeverity.INFO, "Lab results ready",
        event.patientName() + "'s " + event.testName() + " results just came in."));
  }

  @ApplicationModuleListener
  void on(MedicationLowStockEvent event) {
    repository.save(new Notification(event.hospitalId(), NotificationCategory.PHARMACY,
        NotificationSeverity.WARNING, "Low stock alert",
        "Pharmacy flagged " + event.name() + " running low (" + event.stockQuantity()
            + " left, reorder at " + event.reorderLevel() + ")."));
  }
}
