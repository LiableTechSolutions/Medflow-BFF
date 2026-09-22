package com.medflow.modules.patients.application;

import com.medflow.modules.patients.api.HospitalisationStatus;
import com.medflow.modules.patients.api.MappingRelation;
import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.modules.patients.api.request.AddMedicalHistoryRequest;
import com.medflow.modules.patients.api.request.AddPatientReportRequest;
import com.medflow.modules.patients.api.request.AdmitPatientRequest;
import com.medflow.modules.patients.api.request.CreateDailyAnalysisRequest;
import com.medflow.modules.patients.api.request.CreatePatientRequest;
import com.medflow.modules.patients.api.request.DischargePatientRequest;
import com.medflow.modules.patients.api.request.LinkPatientAccountRequest;
import com.medflow.modules.patients.api.request.UpdatePatientRequest;
import com.medflow.modules.patients.api.response.DailyAnalysisResponse;
import com.medflow.modules.patients.api.response.HospitalisationRecordResponse;
import com.medflow.modules.patients.api.response.MedicalHistoryResponse;
import com.medflow.modules.patients.api.response.PatientAccountResponse;
import com.medflow.modules.patients.api.response.PatientClinicalSummaryResponse;
import com.medflow.modules.patients.api.response.PatientReportResponse;
import com.medflow.modules.patients.api.response.PatientResponse;
import com.medflow.modules.patients.domain.entity.DailyAnalysis;
import com.medflow.modules.patients.domain.entity.HospitalisationRecord;
import com.medflow.modules.patients.domain.entity.Patient;
import com.medflow.modules.patients.domain.entity.PatientMedicalHistory;
import com.medflow.modules.patients.domain.entity.PatientReport;
import com.medflow.modules.patients.domain.entity.UserPatientMapping;
import com.medflow.modules.patients.domain.repository.DailyAnalysisRepository;
import com.medflow.modules.patients.domain.repository.HospitalisationRecordRepository;
import com.medflow.modules.patients.domain.repository.PatientMedicalHistoryRepository;
import com.medflow.modules.patients.domain.repository.PatientReportRepository;
import com.medflow.modules.patients.domain.repository.PatientRepository;
import com.medflow.modules.patients.domain.repository.UserPatientMappingRepository;
import com.medflow.modules.patients.domain.service.PatientRegistrationProfileValidator;
import com.medflow.modules.users.api.UserAccountService;
import com.medflow.modules.users.api.UserSummary;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.domain.AccountStatus;
import com.medflow.shared.exception.BusinessRuleViolationException;
import com.medflow.shared.exception.DuplicateResourceException;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.Collection;
import java.util.List;
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
  private final HospitalisationRecordRepository hospitalisationRepository;
  private final DailyAnalysisRepository dailyAnalysisRepository;
  private final UserAccountService userAccountService;
  private final PatientRegistrationProfileValidator profileValidator;

  PatientServiceImpl(PatientRepository repository,
      PatientMedicalHistoryRepository historyRepository,
      PatientReportRepository reportRepository, UserPatientMappingRepository mappingRepository,
      HospitalisationRecordRepository hospitalisationRepository,
      DailyAnalysisRepository dailyAnalysisRepository,
      UserAccountService userAccountService, PatientRegistrationProfileValidator profileValidator) {
    this.repository = repository;
    this.historyRepository = historyRepository;
    this.reportRepository = reportRepository;
    this.mappingRepository = mappingRepository;
    this.hospitalisationRepository = hospitalisationRepository;
    this.dailyAnalysisRepository = dailyAnalysisRepository;
    this.userAccountService = userAccountService;
    this.profileValidator = profileValidator;
  }

  @Override
  @Transactional
  public PatientResponse create(Long hospitalId, CreatePatientRequest request) {
    profileValidator.validateCreate(hospitalId, request);
    if (request.email() != null
        && repository.existsByHospitalIdAndEmailIgnoreCase(hospitalId, request.email())) {
      throw new DuplicateResourceException("A patient with this email already exists");
    }
    var patient = repository.save(new Patient(hospitalId, nextPatientCode(hospitalId),
        request.firstName(), request.lastName(), request.gender(), request.dateOfBirth(),
        request.bloodGroup(), request.phone(), request.email(), request.address(),
        request.emergencyContactName(), request.emergencyContactPhone(), request.city(), request.state(),
        request.postalCode(), request.preferredLanguage(), request.emergencyContactRelationship(),
        request.insuranceProvider(), request.memberId(), request.governmentIdType(),
        request.governmentIdNumber(), request.allergies(), request.consentStatus(),
        request.referringPhysician(), request.guardianName(), request.guardianRelationship(),
        request.guardianMobile()));

    if (Boolean.TRUE.equals(request.isHospitalised())) {
      if (request.hospitalisation() == null) {
        throw new BusinessRuleViolationException(
            "Admission details are required when creating a hospitalised patient");
      }
      admit(hospitalId, patient, request.hospitalisation());
    }
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
    profileValidator.validateUpdate(hospitalId, request);
    if (request.email() != null && repository.existsByHospitalIdAndEmailIgnoreCaseAndIdNot(
        hospitalId, request.email(), patientId)) {
      throw new DuplicateResourceException("A patient with this email already exists");
    }
    var patient = load(hospitalId, patientId);
    patient.update(request);
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
            patient.getPatientCode(), patient.getFullName(), patient.getPhone(),
            patient.getEmail()))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long countByHospital(Long hospitalId) {
    return repository.countByHospitalIdAndDeletedFalse(hospitalId);
  }

  @Override
  @Transactional
  public HospitalisationRecordResponse admitPatient(Long hospitalId, Long patientId,
      AdmitPatientRequest request) {
    var patient = load(hospitalId, patientId);
    if (hospitalisationRepository
        .findFirstByPatientIdAndStatusOrderByAdmissionDateDesc(patientId, HospitalisationStatus.ADMITTED)
        .isPresent()) {
      throw new BusinessRuleViolationException("Patient already has an active hospitalisation");
    }
    var record = admit(hospitalId, patient, request);
    return toResponse(record);
  }

  @Override
  @Transactional
  public HospitalisationRecordResponse dischargePatient(Long hospitalId, Long patientId,
      DischargePatientRequest request) {
    var patient = load(hospitalId, patientId);
    var record = hospitalisationRepository
        .findFirstByPatientIdAndStatusOrderByAdmissionDateDesc(patientId, HospitalisationStatus.ADMITTED)
        .orElseThrow(() -> new BusinessRuleViolationException(
            "Patient has no active hospitalisation to discharge"));
    record.discharge(request.dischargeDate());
    patient.dischargeFromHospital();
    return toResponse(record);
  }

  @Override
  @Transactional
  public DailyAnalysisResponse addDailyAnalysis(Long hospitalId, Long patientId,
      CreateDailyAnalysisRequest request) {
    load(hospitalId, patientId);
    var record = hospitalisationRepository
        .findFirstByPatientIdAndStatusOrderByAdmissionDateDesc(patientId, HospitalisationStatus.ADMITTED)
        .orElseThrow(() -> new BusinessRuleViolationException(
            "Daily analysis requires an active hospitalisation record"));
    var entry = dailyAnalysisRepository.save(new DailyAnalysis(record.getId(), patientId,
        request.bloodPressure(), request.pulse(), request.temperature(), request.spo2(),
        request.notes(), request.recordedByDoctorId()));
    return toResponse(entry);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DailyAnalysisResponse> dailyAnalyses(Long hospitalId, Long patientId) {
    load(hospitalId, patientId);
    return dailyAnalysisRepository.findByPatientIdOrderByRecordedAtAsc(patientId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PatientClinicalSummaryResponse summary(Long hospitalId, Long patientId) {
    var patient = load(hospitalId, patientId);
    var currentRecord = hospitalisationRepository
        .findFirstByPatientIdOrderByAdmissionDateDesc(patientId).orElse(null);
    var dailyEntries = currentRecord == null
        ? List.<DailyAnalysisResponse>of()
        : dailyAnalysisRepository
            .findByHospitalisationRecordIdOrderByRecordedAtAsc(currentRecord.getId()).stream()
            .map(this::toResponse)
            .toList();
    return new PatientClinicalSummaryResponse(toResponse(patient), patient.isHospitalised(),
        currentRecord == null ? null : toResponse(currentRecord), dailyEntries);
  }

  /** Shared by patient creation (inline admission) and the standalone admit endpoint. */
  private HospitalisationRecord admit(Long hospitalId, Patient patient, AdmitPatientRequest request) {
    var record = hospitalisationRepository.save(new HospitalisationRecord(hospitalId,
        patient.getId(), request.admissionDate(), request.ward(), request.bed(),
        request.admittingDoctorId()));
    patient.admit();
    return record;
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
        patient.getEmergencyContactName(), patient.getEmergencyContactPhone(), patient.getCity(),
        patient.getState(), patient.getPostalCode(), patient.getPreferredLanguage(),
        patient.getEmergencyContactRelationship(), patient.getInsuranceProvider(), patient.getMemberId(),
        patient.getGovernmentIdType(), patient.getGovernmentIdNumber(), patient.getAllergies(),
        patient.getConsentStatus(), patient.getReferringPhysician(), patient.getGuardianName(),
        patient.getGuardianRelationship(), patient.getGuardianMobile(), patient.getStatus(),
        patient.isHospitalised(), patient.getCreatedAt(), patient.getUpdatedAt());
  }

  private HospitalisationRecordResponse toResponse(HospitalisationRecord record) {
    return new HospitalisationRecordResponse(record.getId(), record.getPatientId(),
        record.getHospitalId(), record.getAdmissionDate(), record.getDischargeDate(),
        record.getWard(), record.getBed(), record.getAdmittingDoctorId(), record.getStatus(),
        record.getCreatedAt(), record.getUpdatedAt());
  }

  private DailyAnalysisResponse toResponse(DailyAnalysis entry) {
    return new DailyAnalysisResponse(entry.getId(), entry.getHospitalisationRecordId(),
        entry.getPatientId(), entry.getBloodPressure(), entry.getPulse(), entry.getTemperature(),
        entry.getSpo2(), entry.getNotes(), entry.getRecordedByDoctorId(), entry.getRecordedAt());
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
