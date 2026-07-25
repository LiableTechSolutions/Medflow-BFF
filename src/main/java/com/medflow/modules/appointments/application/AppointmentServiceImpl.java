package com.medflow.modules.appointments.application;

import com.medflow.modules.appointments.api.AppointmentBookedEvent;
import com.medflow.modules.appointments.api.AppointmentScheduleConflictEvent;
import com.medflow.modules.appointments.api.AppointmentService;
import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.modules.appointments.api.DailyAppointmentCount;
import com.medflow.modules.appointments.api.request.BookAppointmentRequest;
import com.medflow.modules.appointments.api.request.RescheduleAppointmentRequest;
import com.medflow.modules.appointments.api.response.AppointmentResponse;
import com.medflow.modules.appointments.domain.entity.Appointment;
import com.medflow.modules.appointments.domain.repository.AppointmentRepository;
import com.medflow.modules.appointments.domain.repository.AppointmentSpecifications;
import com.medflow.modules.appointments.mapper.AppointmentMapper;
import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.doctors.api.DoctorSummary;
import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AppointmentServiceImpl implements AppointmentService {

  private static final int DEFAULT_DURATION_MINUTES = 30;
  private static final int MAX_PAGE_SIZE = 100;

  private final AppointmentRepository repository;
  private final AppointmentMapper mapper;
  private final PatientService patientService;
  private final DoctorService doctorService;
  private final ApplicationEventPublisher eventPublisher;

  AppointmentServiceImpl(AppointmentRepository repository, AppointmentMapper mapper,
      PatientService patientService, DoctorService doctorService,
      ApplicationEventPublisher eventPublisher) {
    this.repository = repository;
    this.mapper = mapper;
    this.patientService = patientService;
    this.doctorService = doctorService;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public AppointmentResponse book(BookAppointmentRequest request) {
    var patient = requirePatient(request.patientId());
    var doctor = requireDoctor(request.doctorId());
    var duration = request.durationMinutes() != null
        ? request.durationMinutes()
        : DEFAULT_DURATION_MINUTES;

    var appointment = new Appointment(patient.id(), doctor.id(), request.scheduledAt(), duration,
        request.reason(), doctor.consultationFee(), request.notes());
    repository.save(appointment);

    if (hasConflict(appointment)) {
      eventPublisher.publishEvent(new AppointmentScheduleConflictEvent(
          doctor.id(), doctor.fullName(), appointment.getScheduledAt()));
    }
    eventPublisher.publishEvent(new AppointmentBookedEvent(appointment.getId(), patient.id(),
        patient.fullName(), doctor.id(), doctor.fullName(), appointment.getScheduledAt()));

    return mapper.toResponse(appointment, patient.fullName(), doctor.fullName());
  }

  @Override
  @Transactional(readOnly = true)
  public AppointmentResponse findById(UUID appointmentId) {
    var appointment = load(appointmentId);
    return enrich(List.of(appointment)).getFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<AppointmentResponse> search(AppointmentStatus status, UUID doctorId,
      UUID patientId, LocalDate date, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.ASC, "scheduledAt"));
    Page<Appointment> result = repository.findAll(
        AppointmentSpecifications.withFilters(status, doctorId, patientId, date), pageable);
    var responses = enrich(result.getContent());
    return new PageResponse<>(responses, result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages());
  }

  @Override
  @Transactional
  public AppointmentResponse confirm(UUID appointmentId) {
    var appointment = load(appointmentId);
    appointment.confirm();
    return enrich(List.of(appointment)).getFirst();
  }

  @Override
  @Transactional
  public AppointmentResponse complete(UUID appointmentId) {
    var appointment = load(appointmentId);
    appointment.complete();
    return enrich(List.of(appointment)).getFirst();
  }

  @Override
  @Transactional
  public AppointmentResponse cancel(UUID appointmentId) {
    var appointment = load(appointmentId);
    appointment.cancel();
    return enrich(List.of(appointment)).getFirst();
  }

  @Override
  @Transactional
  public AppointmentResponse reschedule(UUID appointmentId, RescheduleAppointmentRequest request) {
    var appointment = load(appointmentId);
    appointment.reschedule(request.newScheduledAt());
    if (hasConflict(appointment)) {
      var doctor = requireDoctor(appointment.getDoctorId());
      eventPublisher.publishEvent(new AppointmentScheduleConflictEvent(
          doctor.id(), doctor.fullName(), appointment.getScheduledAt()));
    }
    return enrich(List.of(appointment)).getFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public long countOnDate(LocalDate date) {
    var start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
    var end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    return repository.countByScheduledAtBetweenAndStatusNot(start, end,
        AppointmentStatus.CANCELLED);
  }

  @Override
  @Transactional(readOnly = true)
  public long countActive() {
    return repository.countByStatusIn(
        EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal completedRevenueBetween(Instant from, Instant to) {
    return repository.sumCompletedFeesBetween(from, to);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DailyAppointmentCount> dailyCounts(LocalDate from, LocalDate to) {
    var start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
    var end = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    var byDay = repository.countDailyBetween(start, end).stream()
        .collect(Collectors.toMap(
            AppointmentRepository.DailyCountProjection::getDay,
            AppointmentRepository.DailyCountProjection::getTotal));
    var series = new ArrayList<DailyAppointmentCount>();
    for (var day = from; !day.isAfter(to); day = day.plusDays(1)) {
      series.add(new DailyAppointmentCount(day, byDay.getOrDefault(day, 0L)));
    }
    return series;
  }

  /** Overlap scan is bounded to the surrounding day, so the in-memory check stays cheap. */
  private boolean hasConflict(Appointment candidate) {
    var windowStart = candidate.getScheduledAt().minusSeconds(24L * 3600);
    var windowEnd = candidate.endsAt().plusSeconds(24L * 3600);
    return repository.findByDoctorIdAndScheduledAtBetween(
            candidate.getDoctorId(), windowStart, windowEnd).stream()
        .filter(existing -> !existing.getId().equals(candidate.getId()))
        .anyMatch(existing -> existing.overlaps(candidate.getScheduledAt(), candidate.endsAt()));
  }

  private List<AppointmentResponse> enrich(List<Appointment> appointments) {
    var patientNames = patientService.summariesByIds(ids(appointments, Appointment::getPatientId))
        .stream().collect(Collectors.toMap(PatientSummary::id, PatientSummary::fullName));
    var doctorNames = doctorService.summariesByIds(ids(appointments, Appointment::getDoctorId))
        .stream().collect(Collectors.toMap(DoctorSummary::id, DoctorSummary::fullName));
    return appointments.stream()
        .map(appointment -> mapper.toResponse(appointment,
            patientNames.getOrDefault(appointment.getPatientId(), "Unknown patient"),
            doctorNames.getOrDefault(appointment.getDoctorId(), "Unknown doctor")))
        .toList();
  }

  private Set<UUID> ids(List<Appointment> appointments, Function<Appointment, UUID> extractor) {
    return appointments.stream().map(extractor).collect(Collectors.toSet());
  }

  private PatientSummary requirePatient(UUID patientId) {
    return patientService.summariesByIds(List.of(patientId)).stream().findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
  }

  private DoctorSummary requireDoctor(UUID doctorId) {
    return doctorService.summariesByIds(List.of(doctorId)).stream().findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
  }

  private Appointment load(UUID appointmentId) {
    return repository.findById(appointmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));
  }
}
