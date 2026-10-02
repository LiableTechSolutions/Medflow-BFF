package com.medflow.modules.appointments.application;

import com.medflow.modules.appointments.api.AppointmentBookedEvent;
import com.medflow.modules.appointments.api.AppointmentMode;
import com.medflow.modules.appointments.api.AppointmentScheduleConflictEvent;
import com.medflow.modules.appointments.api.AppointmentService;
import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.modules.appointments.api.DailyAppointmentCount;
import com.medflow.modules.appointments.api.request.BookAppointmentRequest;
import com.medflow.modules.appointments.api.request.RescheduleAppointmentRequest;
import com.medflow.modules.appointments.api.response.AppointmentResponse;
import com.medflow.modules.appointments.api.response.AvailableSlotsResponse;
import com.medflow.modules.appointments.api.response.PublicQueueBoardResponse;
import com.medflow.modules.appointments.api.response.PublicQueueEntry;
import com.medflow.modules.appointments.api.response.QueueStatusResponse;
import com.medflow.modules.appointments.domain.entity.Appointment;
import com.medflow.modules.appointments.domain.repository.AppointmentRepository;
import com.medflow.modules.appointments.domain.repository.AppointmentSpecifications;
import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.doctors.api.DoctorSummary;
import com.medflow.modules.doctors.api.response.DoctorAvailabilityResponse;
import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.modules.tenancy.api.HospitalService;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AppointmentServiceImpl implements AppointmentService {

  private static final int DEFAULT_DURATION_MINUTES = 30;
  private static final int MAX_PAGE_SIZE = 100;
  private static final EnumSet<AppointmentStatus> ACTIVE_QUEUE_STATUSES = EnumSet.of(
      AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED, AppointmentStatus.CHECKED_IN,
      AppointmentStatus.IN_CONSULTATION);

  private final AppointmentRepository repository;
  private final PatientService patientService;
  private final DoctorService doctorService;
  private final HospitalService hospitalService;
  private final QueueLinkTokenService queueLinkTokenService;
  private final ApplicationEventPublisher eventPublisher;

  AppointmentServiceImpl(AppointmentRepository repository, PatientService patientService,
      DoctorService doctorService, HospitalService hospitalService,
      QueueLinkTokenService queueLinkTokenService, ApplicationEventPublisher eventPublisher) {
    this.repository = repository;
    this.patientService = patientService;
    this.doctorService = doctorService;
    this.hospitalService = hospitalService;
    this.queueLinkTokenService = queueLinkTokenService;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public AppointmentResponse book(Long hospitalId, Long bookedByUserId,
      BookAppointmentRequest request) {
    var patient = requirePatient(hospitalId, request.patientId());
    var doctor = requireDoctor(hospitalId, request.doctorId());
    var duration = request.durationMinutes() != null
        ? request.durationMinutes()
        : DEFAULT_DURATION_MINUTES;
    var mode = request.appointmentMode() != null ? request.appointmentMode() : AppointmentMode.ONLINE;

    var appointment = repository.save(new Appointment(hospitalId, doctor.id(), patient.id(), mode,
        request.scheduledAt(), duration, nextQueueNumber(doctor.id(), request.scheduledAt()),
        bookedByUserId, request.reason(), doctor.consultationFee(), request.notes()));

    // Conflicts are surfaced, not blocked: the front desk decides, not the system.
    if (hasConflict(appointment)) {
      eventPublisher.publishEvent(new AppointmentScheduleConflictEvent(hospitalId, doctor.id(),
          doctor.fullName(), appointment.getScheduledAt()));
    }
    var hospitalCode = hospitalService.summary(hospitalId).hospitalCode();
    var queueLinkToken = queueLinkTokenService.issue(hospitalCode, doctor.id(),
        appointment.getScheduledAt().atZone(ZoneOffset.UTC).toLocalDate());
    eventPublisher.publishEvent(new AppointmentBookedEvent(hospitalId, queueLinkToken,
        appointment.getId(), patient.id(), patient.fullName(), patient.phone(), patient.email(),
        doctor.id(), doctor.fullName(), appointment.getScheduledAt()));

    return toResponse(appointment, patient.fullName(), doctor);
  }

  @Override
  @Transactional(readOnly = true)
  public AppointmentResponse findById(Long hospitalId, Long appointmentId) {
    return enrich(hospitalId, List.of(load(hospitalId, appointmentId))).getFirst();
  }

  /**
   * Ranks this appointment among its doctor's still-active appointments for the same
   * calendar day, ordered by queue number. A closed appointment (completed, cancelled,
   * no-show) reports position 0 - it's no longer "in" the queue.
   */
  @Override
  @Transactional(readOnly = true)
  public QueueStatusResponse queueStatus(Long hospitalId, Long appointmentId) {
    var appointment = load(hospitalId, appointmentId);
    var day = appointment.getScheduledAt().atZone(ZoneOffset.UTC).toLocalDate();
    var start = day.atStartOfDay(ZoneOffset.UTC).toInstant();
    var end = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    var activeForDoctor = repository
        .findByDoctorIdAndScheduledAtBetween(appointment.getDoctorId(), start, end).stream()
        .filter(candidate -> ACTIVE_QUEUE_STATUSES.contains(candidate.getStatus()))
        .sorted((a, b) -> Integer.compare(
            a.getQueueNumber() == null ? Integer.MAX_VALUE : a.getQueueNumber(),
            b.getQueueNumber() == null ? Integer.MAX_VALUE : b.getQueueNumber()))
        .toList();

    var position = 0;
    for (var i = 0; i < activeForDoctor.size(); i++) {
      if (activeForDoctor.get(i).getId().equals(appointment.getId())) {
        position = i + 1;
        break;
      }
    }

    return new QueueStatusResponse(appointment.getId(), appointment.getQueueNumber(),
        appointment.getStatus(), position, Math.max(position - 1, 0), activeForDoctor.size());
  }

  /**
   * Generates the doctor's consulting slots for this date from their configured
   * availability, then drops any that overlap an existing (non-cancelled) booking or
   * have already passed. A specific-date override, when one exists for this date,
   * replaces the recurring day-of-week rule entirely - including an override that marks
   * the day unavailable, which correctly yields no slots even on an otherwise-recurring day.
   */
  @Override
  @Transactional(readOnly = true)
  public AvailableSlotsResponse availableSlots(Long hospitalId, Long doctorId, LocalDate date) {
    var rules = doctorService.availability(hospitalId, doctorId);
    var isoDayOfWeek = date.getDayOfWeek().getValue() % 7; // 0 = Sunday, matching AddAvailabilityRequest

    var overridesForDate = rules.stream()
        .filter(rule -> date.equals(rule.specificDate()))
        .toList();
    var windows = (!overridesForDate.isEmpty() ? overridesForDate : rules.stream()
        .filter(rule -> rule.specificDate() == null && isoDayOfWeek == (rule.dayOfWeek() == null ? -1 : rule.dayOfWeek()))
        .toList())
        .stream()
        .filter(DoctorAvailabilityResponse::available)
        .toList();

    var start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
    var end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    var booked = repository.findByDoctorIdAndScheduledAtBetween(doctorId, start, end).stream()
        .filter(existing -> existing.getStatus() != AppointmentStatus.CANCELLED)
        .toList();
    var now = Instant.now();

    var slots = new ArrayList<Instant>();
    for (var window : windows) {
      var duration = Duration.ofMinutes(window.slotDurationMinutes());
      for (var slotStart = window.startTime(); !slotStart.plus(duration).isAfter(window.endTime());
          slotStart = slotStart.plus(duration)) {
        var slotStartInstant = date.atTime(slotStart).atZone(ZoneOffset.UTC).toInstant();
        var slotEndInstant = slotStartInstant.plus(duration);
        if (slotStartInstant.isBefore(now)) continue;
        var taken = booked.stream().anyMatch(existing -> existing.overlaps(slotStartInstant, slotEndInstant));
        if (!taken) slots.add(slotStartInstant);
      }
    }
    slots.sort(Instant::compareTo);

    return new AvailableSlotsResponse(doctorId, date, slots);
  }

  @Override
  @Transactional(readOnly = true)
  public String issueQueueLinkToken(Long hospitalId, Long doctorId, LocalDate date) {
    requireDoctor(hospitalId, doctorId);
    var hospitalCode = hospitalService.summary(hospitalId).hospitalCode();
    return queueLinkTokenService.issue(hospitalCode, doctorId, date);
  }

  /**
   * No caller identity here - the token is the only credential an anonymous request
   * has. It is signed and verified server-side (never trusted as raw ids), and its
   * hospitalCode/doctorId are still cross-checked against each other via
   * {@code doctorService.findById}, which throws if the doctor doesn't belong to that
   * hospital - defense in depth even though a valid token already implies that.
   */
  @Override
  @Transactional(readOnly = true)
  public PublicQueueBoardResponse publicQueueBoard(String token) {
    var decoded = queueLinkTokenService.verify(token);
    var hospital = hospitalService.summaryByCode(decoded.hospitalCode());
    var doctor = requireDoctor(hospital.id(), decoded.doctorId());
    var doctorId = decoded.doctorId();
    var date = decoded.date();

    var start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
    var end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    var active = repository.findByDoctorIdAndScheduledAtBetween(doctorId, start, end).stream()
        .filter(appointment -> ACTIVE_QUEUE_STATUSES.contains(appointment.getStatus()))
        .sorted((a, b) -> Integer.compare(
            a.getQueueNumber() == null ? Integer.MAX_VALUE : a.getQueueNumber(),
            b.getQueueNumber() == null ? Integer.MAX_VALUE : b.getQueueNumber()))
        .toList();

    var patientNames = patientService.summariesByIds(hospital.id(),
            active.stream().map(Appointment::getPatientId).collect(Collectors.toSet())).stream()
        .collect(Collectors.toMap(PatientSummary::id, PatientSummary::fullName));

    var nowServing = active.stream()
        .filter(appointment -> appointment.getStatus() == AppointmentStatus.IN_CONSULTATION)
        .findFirst();

    var upcoming = active.stream()
        .filter(appointment -> appointment.getStatus() != AppointmentStatus.IN_CONSULTATION)
        .map(appointment -> new PublicQueueEntry(appointment.getQueueNumber(),
            patientNames.getOrDefault(appointment.getPatientId(), "Patient"), appointment.getStatus(),
            appointment.getScheduledAt()))
        .toList();

    return new PublicQueueBoardResponse(hospital.name(), doctor.fullName(), doctor.specialty(), date,
        nowServing.map(Appointment::getQueueNumber).orElse(null),
        nowServing.map(a -> patientNames.getOrDefault(a.getPatientId(), "Patient")).orElse(null),
        active.size(), upcoming);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<AppointmentResponse> search(Long hospitalId, AppointmentStatus status,
      Long doctorId, Long patientId, LocalDate date, LocalDate from, LocalDate to,
      boolean latestFirst, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(latestFirst ? Sort.Direction.DESC : Sort.Direction.ASC, "scheduledAt"));
    var result = repository.findAll(
        AppointmentSpecifications.withFilters(hospitalId, status, doctorId, patientId, date,
            from, to),
        pageable);
    return new PageResponse<>(enrich(hospitalId, result.getContent()), result.getNumber(),
        result.getSize(), result.getTotalElements(), result.getTotalPages());
  }

  @Override
  @Transactional
  public AppointmentResponse transition(Long hospitalId, Long appointmentId,
      AppointmentStatus target) {
    var appointment = load(hospitalId, appointmentId);
    appointment.transitionTo(target);
    return enrich(hospitalId, List.of(appointment)).getFirst();
  }

  @Override
  @Transactional
  public AppointmentResponse reschedule(Long hospitalId, Long appointmentId,
      RescheduleAppointmentRequest request) {
    var appointment = load(hospitalId, appointmentId);
    appointment.reschedule(request.newScheduledAt());
    if (hasConflict(appointment)) {
      var doctor = requireDoctor(hospitalId, appointment.getDoctorId());
      eventPublisher.publishEvent(new AppointmentScheduleConflictEvent(hospitalId, doctor.id(),
          doctor.fullName(), appointment.getScheduledAt()));
    }
    return enrich(hospitalId, List.of(appointment)).getFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public long countOnDate(Long hospitalId, LocalDate date) {
    var start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
    var end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    return repository.countByHospitalIdAndScheduledAtBetweenAndStatusNot(hospitalId, start, end,
        AppointmentStatus.CANCELLED);
  }

  @Override
  @Transactional(readOnly = true)
  public long countActive(Long hospitalId) {
    return repository.countByHospitalIdAndStatusIn(hospitalId,
        EnumSet.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED,
            AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_CONSULTATION));
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal completedRevenueBetween(Long hospitalId, Instant from, Instant to) {
    return repository.sumCompletedFeesBetween(hospitalId, from, to);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DailyAppointmentCount> dailyCounts(Long hospitalId, LocalDate from, LocalDate to) {
    var start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
    var end = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    var byDay = repository.countDailyBetween(hospitalId, start, end).stream()
        .collect(Collectors.toMap(AppointmentRepository.DailyCountProjection::getVisitDay,
            AppointmentRepository.DailyCountProjection::getTotal));
    var series = new ArrayList<DailyAppointmentCount>();
    for (var day = from; !day.isAfter(to); day = day.plusDays(1)) {
      series.add(new DailyAppointmentCount(day, byDay.getOrDefault(day, 0L)));
    }
    return series;
  }

  /** Queue numbers restart every day, per doctor, so the waiting room list reads 1, 2, 3… */
  private Integer nextQueueNumber(Long doctorId, Instant scheduledAt) {
    var day = scheduledAt.atZone(ZoneOffset.UTC).toLocalDate();
    var start = day.atStartOfDay(ZoneOffset.UTC).toInstant();
    var end = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    return repository.highestQueueNumber(doctorId, start, end) + 1;
  }

  /** Overlap scan is bounded to the surrounding day, so the in-memory check stays cheap. */
  private boolean hasConflict(Appointment candidate) {
    var windowStart = candidate.getScheduledAt().minusSeconds(24L * 3600);
    var windowEnd = candidate.endsAt().plusSeconds(24L * 3600);
    return repository
        .findByDoctorIdAndScheduledAtBetween(candidate.getDoctorId(), windowStart, windowEnd)
        .stream()
        .filter(existing -> !existing.getId().equals(candidate.getId()))
        .anyMatch(existing -> existing.overlaps(candidate.getScheduledAt(), candidate.endsAt()));
  }

  private List<AppointmentResponse> enrich(Long hospitalId, List<Appointment> appointments) {
    Map<Long, String> patientNames = patientService.summariesByIds(hospitalId,
            appointments.stream().map(Appointment::getPatientId).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(PatientSummary::id, PatientSummary::fullName));
    Map<Long, DoctorSummary> doctors = doctorService.summariesByIds(hospitalId,
            appointments.stream().map(Appointment::getDoctorId).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(DoctorSummary::id, doctor -> doctor));

    return appointments.stream()
        .map(appointment -> toResponse(appointment,
            patientNames.getOrDefault(appointment.getPatientId(), "Unknown patient"),
            doctors.get(appointment.getDoctorId())))
        .toList();
  }

  private AppointmentResponse toResponse(Appointment appointment, String patientName,
      DoctorSummary doctor) {
    return new AppointmentResponse(appointment.getId(), appointment.getHospitalId(),
        appointment.getPatientId(), patientName, appointment.getDoctorId(),
        doctor == null ? "Unknown doctor" : doctor.fullName(),
        doctor == null ? null : doctor.specialty(),
        appointment.getAppointmentMode(), appointment.getScheduledAt(),
        appointment.getDurationMinutes(), appointment.getStatus(), appointment.getQueueNumber(),
        appointment.getBookedByUserId(), appointment.getReason(), appointment.getConsultationFee(),
        appointment.getNotes(), appointment.getCreatedAt(), appointment.getUpdatedAt());
  }

  private PatientSummary requirePatient(Long hospitalId, Long patientId) {
    return patientService.summariesByIds(hospitalId, List.of(patientId)).stream().findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
  }

  private DoctorSummary requireDoctor(Long hospitalId, Long doctorId) {
    return doctorService.summariesByIds(hospitalId, List.of(doctorId)).stream().findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
  }

  private Appointment load(Long hospitalId, Long appointmentId) {
    return repository.findByIdAndHospitalId(appointmentId, hospitalId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Appointment not found: " + appointmentId));
  }
}
