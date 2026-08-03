package com.medflow.modules.doctors.application;

import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.doctors.api.DoctorSummary;
import com.medflow.modules.doctors.api.request.AddAvailabilityRequest;
import com.medflow.modules.doctors.api.request.AssignStaffRequest;
import com.medflow.modules.doctors.api.request.CreateDoctorRequest;
import com.medflow.modules.doctors.api.request.UpdateDoctorRequest;
import com.medflow.modules.doctors.api.response.DoctorAvailabilityResponse;
import com.medflow.modules.doctors.api.response.DoctorResponse;
import com.medflow.modules.doctors.api.response.DoctorStaffResponse;
import com.medflow.modules.doctors.domain.entity.Doctor;
import com.medflow.modules.doctors.domain.entity.DoctorAvailability;
import com.medflow.modules.doctors.domain.entity.UserDoctorMapping;
import com.medflow.modules.doctors.domain.repository.DoctorAvailabilityRepository;
import com.medflow.modules.doctors.domain.repository.DoctorRepository;
import com.medflow.modules.doctors.domain.repository.UserDoctorMappingRepository;
import com.medflow.modules.rbac.api.RoleCodes;
import com.medflow.modules.users.api.UserAccountService;
import com.medflow.modules.users.api.request.CreateUserAccountRequest;
import com.medflow.modules.users.api.request.UpdateUserProfileRequest;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.domain.AccountStatus;
import com.medflow.shared.exception.DuplicateResourceException;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DoctorServiceImpl implements DoctorService {

  private static final int MAX_PAGE_SIZE = 100;
  /** Sentinel so the "matching user ids" IN-list is never empty. */
  private static final Long NO_MATCH = -1L;

  private final DoctorRepository repository;
  private final DoctorAvailabilityRepository availabilityRepository;
  private final UserDoctorMappingRepository staffRepository;
  private final UserAccountService userAccountService;
  private final SecureRandom secureRandom = new SecureRandom();

  DoctorServiceImpl(DoctorRepository repository,
      DoctorAvailabilityRepository availabilityRepository,
      UserDoctorMappingRepository staffRepository, UserAccountService userAccountService) {
    this.repository = repository;
    this.availabilityRepository = availabilityRepository;
    this.staffRepository = staffRepository;
    this.userAccountService = userAccountService;
  }

  /**
   * Onboarding creates the login and the clinical profile together: the reference schema
   * models a doctor as a user with extra professional attributes, so the two must never
   * drift apart.
   */
  @Override
  @Transactional
  public DoctorResponse create(Long hospitalId, CreateDoctorRequest request) {
    if (repository.existsByRegistrationNumber(request.registrationNumber())) {
      throw new DuplicateResourceException(
          "A doctor with this registration number already exists");
    }
    var account = userAccountService.create(hospitalId, new CreateUserAccountRequest(
        request.firstName(), request.lastName(), request.email(), request.phone(),
        request.password() == null || request.password().isBlank()
            ? randomPassword()
            : request.password(),
        RoleCodes.DOCTOR, null, null, null));

    var doctor = repository.save(new Doctor(hospitalId, account.id(), nextDoctorCode(hospitalId),
        request.specialty(), request.qualification(), request.registrationNumber(),
        request.yearsOfExperience(), request.consultationFee(), request.bio()));
    return toResponse(doctor, account);
  }

  @Override
  @Transactional(readOnly = true)
  public DoctorResponse findById(Long hospitalId, Long doctorId) {
    var doctor = load(hospitalId, doctorId);
    return toResponse(doctor, userAccountService.getById(hospitalId, doctor.getUserId()));
  }

  @Override
  @Transactional
  public DoctorResponse update(Long hospitalId, Long doctorId, UpdateDoctorRequest request) {
    var doctor = load(hospitalId, doctorId);
    doctor.update(request.specialty(), request.qualification(), request.yearsOfExperience(),
        request.consultationFee(), request.digitalSignatureUrl(), request.bio());

    var account = userAccountService.getById(hospitalId, doctor.getUserId());
    var updatedAccount = userAccountService.updateProfile(hospitalId, doctor.getUserId(),
        new UpdateUserProfileRequest(request.firstName(), request.lastName(), request.phone(),
            account.gender(), account.dateOfBirth(), account.profilePhotoUrl()));
    return toResponse(doctor, updatedAccount);
  }

  @Override
  @Transactional
  public DoctorResponse changeStatus(Long hospitalId, Long doctorId, AccountStatus status) {
    var doctor = load(hospitalId, doctorId);
    doctor.changeStatus(status);
    // Suspending a doctor must also stop them signing in, so the account follows.
    var account = userAccountService.changeStatus(hospitalId, doctor.getUserId(), status);
    return toResponse(doctor, account);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<DoctorResponse> search(Long hospitalId, String query, String specialty,
      AccountStatus status, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.ASC, "doctorCode"));
    var normalizedQuery = (query == null || query.isBlank()) ? null : query.trim();
    var normalizedSpecialty = (specialty == null || specialty.isBlank()) ? null : specialty.trim();

    var matchingUserIds = new java.util.ArrayList<>(
        userAccountService.findIdsMatching(hospitalId, normalizedQuery));
    matchingUserIds.add(NO_MATCH);

    var result = repository.search(hospitalId, normalizedQuery, normalizedSpecialty, status,
        matchingUserIds, pageable);
    var accounts = accountsFor(result.getContent());
    return new PageResponse<>(
        result.getContent().stream()
            .map(doctor -> toResponse(doctor, accounts.get(doctor.getUserId())))
            .toList(),
        result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
  }

  @Override
  @Transactional(readOnly = true)
  public List<DoctorAvailabilityResponse> availability(Long hospitalId, Long doctorId) {
    load(hospitalId, doctorId);
    return availabilityRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public DoctorAvailabilityResponse addAvailability(Long hospitalId, Long doctorId,
      AddAvailabilityRequest request) {
    load(hospitalId, doctorId);
    var slot = availabilityRepository.save(new DoctorAvailability(doctorId, request.dayOfWeek(),
        request.specificDate(), request.startTime(), request.endTime(),
        request.slotDurationMinutes(), request.available()));
    return toResponse(slot);
  }

  @Override
  @Transactional
  public void removeAvailability(Long hospitalId, Long doctorId, Long availabilityId) {
    load(hospitalId, doctorId);
    var slot = availabilityRepository.findByIdAndDoctorId(availabilityId, doctorId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Availability not found: " + availabilityId));
    availabilityRepository.delete(slot);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DoctorStaffResponse> staff(Long hospitalId, Long doctorId) {
    load(hospitalId, doctorId);
    var mappings = staffRepository.findByDoctorId(doctorId);
    var users = userAccountService.summariesByIds(
            mappings.stream().map(UserDoctorMapping::getUserId).toList()).stream()
        .collect(Collectors.toMap(user -> user.id(), user -> user));
    return mappings.stream().map(mapping -> {
      var user = users.get(mapping.getUserId());
      return new DoctorStaffResponse(mapping.getId(), mapping.getUserId(),
          user == null ? null : user.fullName(), user == null ? null : user.email(),
          mapping.getRelationType(), mapping.isPrimary(), mapping.getCreatedAt());
    }).toList();
  }

  @Override
  @Transactional
  public DoctorStaffResponse assignStaff(Long hospitalId, Long doctorId,
      AssignStaffRequest request) {
    load(hospitalId, doctorId);
    if (staffRepository.existsByUserIdAndDoctorId(request.userId(), doctorId)) {
      throw new DuplicateResourceException("This staff member is already assigned to the doctor");
    }
    var account = userAccountService.getById(hospitalId, request.userId());
    var relationType = (request.relationType() == null || request.relationType().isBlank())
        ? "assistant"
        : request.relationType();
    var mapping = staffRepository.save(new UserDoctorMapping(request.userId(), doctorId,
        hospitalId, relationType, Boolean.TRUE.equals(request.primary())));
    return new DoctorStaffResponse(mapping.getId(), mapping.getUserId(), account.fullName(),
        account.email(), mapping.getRelationType(), mapping.isPrimary(), mapping.getCreatedAt());
  }

  @Override
  @Transactional(readOnly = true)
  public List<DoctorSummary> summariesByIds(Long hospitalId, Collection<Long> doctorIds) {
    if (doctorIds.isEmpty()) {
      return List.of();
    }
    var doctors = repository.findByHospitalIdAndIdIn(hospitalId, doctorIds);
    var names = userAccountService.summariesByIds(
            doctors.stream().map(Doctor::getUserId).filter(java.util.Objects::nonNull).toList())
        .stream().collect(Collectors.toMap(user -> user.id(), user -> user.fullName()));
    return doctors.stream()
        .map(doctor -> new DoctorSummary(doctor.getId(), doctor.getHospitalId(), doctor.getUserId(),
            doctor.getDoctorCode(), names.getOrDefault(doctor.getUserId(), "Unknown doctor"),
            doctor.getSpecialty(), doctor.getConsultationFee()))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long countByHospital(Long hospitalId) {
    return repository.countByHospitalId(hospitalId);
  }

  private Map<Long, UserAccountResponse> accountsFor(List<Doctor> doctors) {
    var userIds = doctors.stream().map(Doctor::getUserId).filter(java.util.Objects::nonNull).toList();
    return userIds.isEmpty()
        ? Map.of()
        : userIds.stream().distinct()
            .map(userAccountService::getById)
            .collect(Collectors.toMap(UserAccountResponse::id, account -> account));
  }

  /** Codes stay readable and stable per tenant: DOC-0001, DOC-0002, … */
  private String nextDoctorCode(Long hospitalId) {
    return "DOC-%04d".formatted(repository.countByHospitalId(hospitalId) + 1);
  }

  private String randomPassword() {
    var bytes = new byte[12];
    secureRandom.nextBytes(bytes);
    return "Mf!" + HexFormat.of().formatHex(bytes);
  }

  private Doctor load(Long hospitalId, Long doctorId) {
    return repository.findByIdAndHospitalId(doctorId, hospitalId)
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
  }

  private DoctorResponse toResponse(Doctor doctor, UserAccountResponse account) {
    return new DoctorResponse(doctor.getId(), doctor.getHospitalId(), doctor.getUserId(),
        doctor.getDoctorCode(),
        account == null ? null : account.firstName(),
        account == null ? null : account.lastName(),
        account == null ? "Unknown doctor" : account.fullName(),
        account == null ? null : account.email(),
        account == null ? null : account.phone(),
        doctor.getSpecialty(), doctor.getQualification(), doctor.getRegistrationNumber(),
        doctor.getYearsOfExperience(), doctor.getConsultationFee(),
        doctor.getDigitalSignatureUrl(), doctor.getBio(), doctor.getStatus(),
        doctor.getCreatedAt(), doctor.getUpdatedAt());
  }

  private DoctorAvailabilityResponse toResponse(DoctorAvailability slot) {
    return new DoctorAvailabilityResponse(slot.getId(), slot.getDoctorId(), slot.getDayOfWeek(),
        slot.getSpecificDate(), slot.getStartTime(), slot.getEndTime(),
        slot.getSlotDurationMinutes(), slot.isAvailable());
  }
}
