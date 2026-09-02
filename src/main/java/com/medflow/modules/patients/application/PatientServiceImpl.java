package com.medflow.modules.patients.application;

import com.medflow.modules.patients.api.MappingRelation;
import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.modules.patients.api.request.AddMedicalHistoryRequest;
import com.medflow.modules.patients.api.request.AddPatientReportRequest;
import com.medflow.modules.patients.api.request.CreatePatientRequest;
import com.medflow.modules.patients.api.request.LinkPatientAccountRequest;
import com.medflow.modules.patients.api.request.UpdatePatientRequest;
import com.medflow.modules.patients.api.response.MedicalHistoryResponse;
import com.medflow.modules.patients.api.response.PatientAccountResponse;
import com.medflow.modules.patients.api.response.PatientReportResponse;
import com.medflow.modules.patients.api.response.PatientResponse;
import com.medflow.modules.patients.domain.entity.Patient;
import com.medflow.modules.patients.domain.entity.PatientMedicalHistory;
import com.medflow.modules.patients.domain.entity.PatientReport;
import com.medflow.modules.patients.domain.entity.UserPatientMapping;
import com.medflow.modules.patients.domain.repository.PatientMedicalHistoryRepository;
import com.medflow.modules.patients.domain.repository.PatientReportRepository;
import com.medflow.modules.patients.domain.repository.PatientRepository;
import com.medflow.modules.patients.domain.repository.UserPatientMappingRepository;
import com.medflow.modules.settings.api.RegistrationFieldState;
import com.medflow.modules.settings.api.RegistrationProfileService;
import com.medflow.modules.users.api.UserAccountService;
import com.medflow.modules.users.api.UserSummary;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.domain.AccountStatus;
import com.medflow.shared.exception.DuplicateResourceException;
import com.medflow.shared.exception.BusinessRuleViolationException;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDate;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PatientServiceImpl implements PatientService {

  private static final int MAX_PAGE_SIZE = 100;

  private final PatientRepository repository;
  private final PatientMedicalHistoryRepository historyRepository;
  private final PatientReportRepository reportRepository;
  private final UserPatientMappingRepository mappingRepository;
  private final UserAccountService userAccountService;
  private final RegistrationProfileService registrationProfileService;

  PatientServiceImpl(PatientRepository repository,
      PatientMedicalHistoryRepository historyRepository,
      PatientReportRepository reportRepository, UserPatientMappingRepository mappingRepository,
      UserAccountService userAccountService, RegistrationProfileService registrationProfileService) {
    this.repository = repository;
    this.historyRepository = historyRepository;
    this.reportRepository = reportRepository;
    this.mappingRepository = mappingRepository;
    this.userAccountService = userAccountService;
    this.registrationProfileService = registrationProfileService;
  }

  @Override
  @Transactional
  public PatientResponse create(Long hospitalId, CreatePatientRequest request) {
    var states = registrationProfileService.statesFor(hospitalId);
    var registrationData = validateAndFilter(states, request.registrationData(), request.firstName(),
      request.lastName(), request.dateOfBirth(), request.phone(), request.email());
    if (request.email() != null
        && repository.existsByHospitalIdAndEmailIgnoreCase(hospitalId, request.email())) {
      throw new DuplicateResourceException("A patient with this email already exists");
    }
    var patient = repository.save(new Patient(hospitalId, nextPatientCode(hospitalId),
        request.firstName(), request.lastName(), request.gender(), request.dateOfBirth(),
        request.bloodGroup(), request.phone(), request.email(), request.address(),
        request.emergencyContactName(), request.emergencyContactPhone()));
      patient.setRegistrationData(registrationData);
    return toResponse(patient);
  }

  @Override
  @Transactional(readOnly = true)
  public PatientResponse findById(Long hospitalId, Long patientId) {
    return toResponse(load(hospitalId, patientId));
  }

  @Override
  @Transactional
  public PatientResponse update(Long hospitalId, Long patientId, UpdatePatientRequest request) {
    var states = registrationProfileService.statesFor(hospitalId);
    var registrationData = validateAndFilter(states, request.registrationData(), request.firstName(),
      request.lastName(), request.dateOfBirth(), request.phone(), request.email());
    if (request.email() != null && repository.existsByHospitalIdAndEmailIgnoreCaseAndIdNot(
        hospitalId, request.email(), patientId)) {
      throw new DuplicateResourceException("A patient with this email already exists");
    }
    var patient = load(hospitalId, patientId);
    patient.update(request);
    patient.setRegistrationData(registrationData);
    return toResponse(patient);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<PatientResponse> search(Long hospitalId, String query, AccountStatus status,
      int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var normalized = (query == null || query.isBlank()) ? null : query.trim();
    return PageResponse.from(
        repository.search(hospitalId, normalized, status, pageable).map(this::toResponse));
  }

  @Override
  @Transactional(readOnly = true)
  public List<MedicalHistoryResponse> medicalHistory(Long hospitalId, Long patientId) {
    load(hospitalId, patientId);
    return historyRepository.findByPatientIdOrderByRecordedAtDesc(patientId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public MedicalHistoryResponse addMedicalHistory(Long hospitalId, Long patientId,
      AddMedicalHistoryRequest request) {
    load(hospitalId, patientId);
    var entry = historyRepository.save(new PatientMedicalHistory(patientId,
        request.conditionName(), request.notes(), request.recordedByDoctorId()));
    return toResponse(entry);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PatientReportResponse> reports(Long hospitalId, Long patientId) {
    load(hospitalId, patientId);
    return reportRepository.findByPatientIdOrderByUploadedAtDesc(patientId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public PatientReportResponse addReport(Long hospitalId, Long patientId, Long uploadedByUserId,
      AddPatientReportRequest request) {
    load(hospitalId, patientId);
    var report = reportRepository.save(new PatientReport(patientId, hospitalId,
        request.reportType(), request.fileUrl(), uploadedByUserId));
    return toResponse(report);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PatientAccountResponse> linkedAccounts(Long hospitalId, Long patientId) {
    load(hospitalId, patientId);
    var mappings = mappingRepository.findByPatientId(patientId);
    var users = userAccountService.summariesByIds(
            mappings.stream().map(UserPatientMapping::getUserId).toList()).stream()
        .collect(Collectors.toMap(UserSummary::id, user -> user));
    return mappings.stream()
        .map(mapping -> {
          var user = users.get(mapping.getUserId());
          return new PatientAccountResponse(mapping.getId(), mapping.getUserId(),
              user == null ? null : user.fullName(), user == null ? null : user.email(),
              mapping.getRelation(), mapping.isPrimaryContact(), mapping.getCreatedAt());
        })
        .toList();
  }

  @Override
  @Transactional
  public PatientAccountResponse linkAccount(Long hospitalId, Long patientId,
      LinkPatientAccountRequest request) {
    var patient = load(hospitalId, patientId);
    if (mappingRepository.existsByUserIdAndPatientId(request.userId(), patientId)) {
      throw new DuplicateResourceException("This account is already linked to the patient");
    }
    var user = userAccountService.getById(hospitalId, request.userId());
    var mapping = mappingRepository.save(new UserPatientMapping(request.userId(), patientId,
        request.relation(), Boolean.TRUE.equals(request.primaryContact())));

    // A patient acting for themselves also owns the record, which unlocks their portal.
    if (request.relation() == MappingRelation.SELF && patient.getUserId() == null) {
      patient.linkAccount(request.userId());
    }
    return new PatientAccountResponse(mapping.getId(), mapping.getUserId(), user.fullName(),
        user.email(), mapping.getRelation(), mapping.isPrimaryContact(), mapping.getCreatedAt());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PatientSummary> summariesByIds(Long hospitalId, Collection<Long> patientIds) {
    if (patientIds.isEmpty()) {
      return List.of();
    }
    return repository.findByHospitalIdAndIdIn(hospitalId, patientIds).stream()
        .map(patient -> new PatientSummary(patient.getId(), patient.getHospitalId(),
            patient.getPatientCode(), patient.getFullName(), patient.getPhone()))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long countByHospital(Long hospitalId) {
    return repository.countByHospitalIdAndDeletedFalse(hospitalId);
  }

  /** Readable, per-tenant sequence: PAT-000001, PAT-000002, … */
  private String nextPatientCode(Long hospitalId) {
    return "PAT-%06d".formatted(repository.countByHospitalIdAndDeletedFalse(hospitalId) + 1);
  }

  private Patient load(Long hospitalId, Long patientId) {
    return repository.findByIdAndHospitalIdAndDeletedFalse(patientId, hospitalId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
  }

  private PatientResponse toResponse(Patient patient) {
    return new PatientResponse(patient.getId(), patient.getHospitalId(), patient.getUserId(),
        patient.getPatientCode(), patient.getFirstName(), patient.getLastName(),
        patient.getFullName(), patient.getGender(), patient.getDateOfBirth(), patient.getAge(),
        patient.getBloodGroup(), patient.getPhone(), patient.getEmail(), patient.getAddress(),
        patient.getEmergencyContactName(), patient.getEmergencyContactPhone(), patient.getStatus(),
        patient.getCreatedAt(), patient.getUpdatedAt(), patient.getRegistrationData());
  }

  private Map<String, Object> validateAndFilter(Map<String, RegistrationFieldState> states,
      Map<String, Object> submitted, String firstName, String lastName, LocalDate dateOfBirth,
      String phone, String email) {
    var values = new LinkedHashMap<String, Object>();
    if (submitted != null) {
      values.putAll(submitted);
    }
    values.putIfAbsent("first_name", firstName);
    values.putIfAbsent("last_name", lastName);
    values.putIfAbsent("date_of_birth", dateOfBirth);
    values.putIfAbsent("contact_number", phone);
    values.putIfAbsent("email", email);
    for (var entry : states.entrySet()) {
      if (entry.getValue() == RegistrationFieldState.REQUIRED
          && isBlank(values.get(entry.getKey()))) {
        throw new BusinessRuleViolationException("Required patient registration field is missing: "
            + entry.getKey());
      }
      if (entry.getValue() == RegistrationFieldState.HIDDEN) {
        values.remove(entry.getKey());
      }
    }
    return values;
  }

  private boolean isBlank(Object value) {
    return value == null || (value instanceof String text && text.isBlank());
  }

  private MedicalHistoryResponse toResponse(PatientMedicalHistory entry) {
    return new MedicalHistoryResponse(entry.getId(), entry.getPatientId(), entry.getConditionName(),
        entry.getNotes(), entry.getRecordedByDoctorId(), entry.getRecordedAt());
  }

  private PatientReportResponse toResponse(PatientReport report) {
    return new PatientReportResponse(report.getId(), report.getPatientId(), report.getReportType(),
        report.getFileUrl(), report.getUploadedByUserId(), report.getUploadedAt());
  }
}
