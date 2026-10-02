package com.medflow.modules.prescriptions.application;

import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.doctors.api.DoctorSummary;
import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.modules.prescriptions.api.PrescriptionFollowUpDueEvent;
import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.modules.prescriptions.domain.entity.Prescription;
import com.medflow.modules.prescriptions.domain.repository.PrescriptionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Once a day, finds every active prescription whose follow-up date is tomorrow and raises
 * one reminder event per patient — the notifications module turns that into the actual
 * email/WhatsApp/SMS send, same as it does for a booking confirmation.
 */
@Component
class PrescriptionFollowUpReminderScheduler {

  private static final Logger log = LoggerFactory.getLogger(PrescriptionFollowUpReminderScheduler.class);

  private final PrescriptionRepository repository;
  private final PatientService patientService;
  private final DoctorService doctorService;
  private final ApplicationEventPublisher eventPublisher;

  PrescriptionFollowUpReminderScheduler(PrescriptionRepository repository,
      PatientService patientService, DoctorService doctorService,
      ApplicationEventPublisher eventPublisher) {
    this.repository = repository;
    this.patientService = patientService;
    this.doctorService = doctorService;
    this.eventPublisher = eventPublisher;
  }

  /** Runs daily at 08:00 server time. */
  @Scheduled(cron = "0 0 8 * * *")
  void sendDueReminders() {
    var tomorrow = LocalDate.now().plusDays(1);
    var due = repository.findByFollowUpDateAndStatus(tomorrow, PrescriptionStatus.ACTIVE);
    if (due.isEmpty()) {
      return;
    }
    log.info("Sending {} follow-up reminder(s) for {}", due.size(), tomorrow);

    // Batched per hospital so each patient/doctor lookup stays scoped to its own tenant,
    // same as every other cross-module call in this app.
    var byHospital = due.stream().collect(Collectors.groupingBy(Prescription::getHospitalId));
    byHospital.forEach(this::sendForHospital);
  }

  private void sendForHospital(Long hospitalId, List<Prescription> prescriptions) {
    Map<Long, PatientSummary> patients = patientService
        .summariesByIds(hospitalId, prescriptions.stream().map(Prescription::getPatientId).toList())
        .stream().collect(Collectors.toMap(PatientSummary::id, p -> p));
    Map<Long, DoctorSummary> doctors = doctorService
        .summariesByIds(hospitalId, prescriptions.stream().map(Prescription::getDoctorId).toList())
        .stream().collect(Collectors.toMap(DoctorSummary::id, d -> d));

    for (var prescription : prescriptions) {
      var patient = patients.get(prescription.getPatientId());
      var doctor = doctors.get(prescription.getDoctorId());
      if (patient == null) {
        continue;
      }
      eventPublisher.publishEvent(new PrescriptionFollowUpDueEvent(hospitalId, prescription.getId(),
          patient.fullName(), patient.phone(), patient.email(),
          doctor == null ? "your doctor" : doctor.fullName(), prescription.getFollowUpDate()));
    }
  }
}
