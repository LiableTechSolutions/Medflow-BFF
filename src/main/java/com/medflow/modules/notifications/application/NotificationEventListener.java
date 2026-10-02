package com.medflow.modules.notifications.application;

import com.medflow.modules.appointments.api.AppointmentBookedEvent;
import com.medflow.modules.appointments.api.AppointmentScheduleConflictEvent;
import com.medflow.modules.laboratory.api.LabOrderCompletedEvent;
import com.medflow.modules.notifications.api.NotificationCategory;
import com.medflow.modules.notifications.api.NotificationSeverity;
import com.medflow.modules.notifications.domain.entity.Notification;
import com.medflow.modules.notifications.domain.repository.NotificationRepository;
import com.medflow.modules.pharmacy.api.MedicationLowStockEvent;
import com.medflow.modules.prescriptions.api.PrescriptionFollowUpDueEvent;
import com.medflow.modules.prescriptions.api.PrescriptionSendRequestedEvent;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("MMM d, HH:mm 'UTC'").withZone(ZoneOffset.UTC);
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

  private final NotificationRepository repository;
  private final NotificationProperties properties;
  private final EmailChannelSender emailSender;
  private final TwilioChannelSender twilioSender;

  NotificationEventListener(NotificationRepository repository, NotificationProperties properties,
      EmailChannelSender emailSender, TwilioChannelSender twilioSender) {
    this.repository = repository;
    this.properties = properties;
    this.emailSender = emailSender;
    this.twilioSender = twilioSender;
  }

  /**
   * Always raises the in-app notification. Email/WhatsApp/SMS are attempted too, but
   * each channel sender logs a mock line instead of a real send until its own account is
   * configured (SMTP password for email, Twilio Account SID + Auth Token for
   * WhatsApp/SMS) — see EmailChannelSender / TwilioChannelSender.
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
    emailSender.send(event.patientEmail(), "Your appointment is booked", message);
    twilioSender.sendWhatsApp(event.patientPhone(), message);
    twilioSender.sendSms(event.patientPhone(), message);
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

  /** Staff explicitly chose to send this prescription — never triggered by viewing/printing. */
  @ApplicationModuleListener
  void on(PrescriptionSendRequestedEvent event) {
    var message = "Prescription from " + event.doctorName()
        + (event.diagnosis() == null || event.diagnosis().isBlank() ? "" : " (" + event.diagnosis() + ")")
        + ": " + (event.medicinesSummary().isBlank() ? "no medicines recorded" : event.medicinesSummary());
    repository.save(new Notification(event.hospitalId(), NotificationCategory.SYSTEM,
        NotificationSeverity.INFO, "Prescription sent", message));
    emailSender.send(event.patientEmail(), "Your prescription from " + event.doctorName(), message);
    twilioSender.sendWhatsApp(event.patientPhone(), message);
    twilioSender.sendSms(event.patientPhone(), message);
  }

  /** Raised by the daily follow-up scheduler, one day before the patient is due back. */
  @ApplicationModuleListener
  void on(PrescriptionFollowUpDueEvent event) {
    var message = "Reminder: your follow-up with " + event.doctorName() + " is due tomorrow, "
        + DATE_FORMAT.format(event.followUpDate()) + ".";
    repository.save(new Notification(event.hospitalId(), NotificationCategory.SYSTEM,
        NotificationSeverity.INFO, "Follow-up reminder", message));
    emailSender.send(event.patientEmail(), "Follow-up reminder", message);
    twilioSender.sendWhatsApp(event.patientPhone(), message);
    twilioSender.sendSms(event.patientPhone(), message);
  }
}
