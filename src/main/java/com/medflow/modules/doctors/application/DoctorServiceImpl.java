package com.medflow.modules.doctors.application;

import com.medflow.modules.doctors.api.DoctorAvailability;
import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.doctors.api.DoctorSummary;
import com.medflow.modules.doctors.api.request.CreateDoctorRequest;
import com.medflow.modules.doctors.api.request.UpdateDoctorRequest;
import com.medflow.modules.doctors.api.response.DoctorResponse;
import com.medflow.modules.doctors.domain.entity.Doctor;
import com.medflow.modules.doctors.domain.repository.DoctorRepository;
import com.medflow.modules.doctors.mapper.DoctorMapper;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.exception.DuplicateResourceException;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DoctorServiceImpl implements DoctorService {

  private static final int MAX_PAGE_SIZE = 100;

  private final DoctorRepository repository;
  private final DoctorMapper mapper;

  DoctorServiceImpl(DoctorRepository repository, DoctorMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public DoctorResponse create(CreateDoctorRequest request) {
    if (repository.existsByEmailIgnoreCase(request.email())) {
      throw new DuplicateResourceException("A doctor with this email already exists");
    }
    if (repository.existsByLicenseNumber(request.licenseNumber())) {
      throw new DuplicateResourceException("A doctor with this license number already exists");
    }
    var doctor = new Doctor(request.fullName(), request.email(), request.phone(),
        request.specialty(), request.department(), request.licenseNumber(),
        request.consultationFee());
    return mapper.toResponse(repository.save(doctor));
  }

  @Override
  @Transactional(readOnly = true)
  public DoctorResponse findById(UUID doctorId) {
    return mapper.toResponse(load(doctorId));
  }

  @Override
  @Transactional
  public DoctorResponse update(UUID doctorId, UpdateDoctorRequest request) {
    if (repository.existsByEmailIgnoreCaseAndIdNot(request.email(), doctorId)) {
      throw new DuplicateResourceException("A doctor with this email already exists");
    }
    var doctor = load(doctorId);
    doctor.update(request.fullName(), request.email(), request.phone(), request.specialty(),
        request.department(), request.consultationFee());
    return mapper.toResponse(doctor);
  }

  @Override
  @Transactional
  public DoctorResponse changeAvailability(UUID doctorId, DoctorAvailability availability) {
    var doctor = load(doctorId);
    doctor.changeAvailability(availability);
    return mapper.toResponse(doctor);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<DoctorResponse> search(String query, String specialty,
      DoctorAvailability availability, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.ASC, "fullName"));
    var normalizedQuery = (query == null || query.isBlank()) ? null : query.trim();
    var normalizedSpecialty = (specialty == null || specialty.isBlank()) ? null : specialty.trim();
    return PageResponse.from(
        repository.search(normalizedQuery, normalizedSpecialty, availability, pageable)
            .map(mapper::toResponse));
  }

  @Override
  @Transactional(readOnly = true)
  public List<DoctorSummary> summariesByIds(Collection<UUID> doctorIds) {
    return repository.findAllById(doctorIds).stream().map(mapper::toSummary).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long count() {
    return repository.count();
  }

  private Doctor load(UUID doctorId) {
    return repository.findById(doctorId)
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
  }
}
