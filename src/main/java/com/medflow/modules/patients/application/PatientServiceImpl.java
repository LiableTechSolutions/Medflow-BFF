package com.medflow.modules.patients.application;

import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.modules.patients.api.request.CreatePatientRequest;
import com.medflow.modules.patients.api.request.UpdatePatientRequest;
import com.medflow.modules.patients.api.response.PatientResponse;
import com.medflow.modules.patients.domain.entity.Patient;
import com.medflow.modules.patients.domain.repository.PatientRepository;
import com.medflow.modules.patients.mapper.PatientMapper;
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
class PatientServiceImpl implements PatientService {

  private static final int MAX_PAGE_SIZE = 100;

  private final PatientRepository repository;
  private final PatientMapper mapper;

  PatientServiceImpl(PatientRepository repository, PatientMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public PatientResponse create(CreatePatientRequest request) {
    if (request.email() != null && repository.existsByEmail(request.email())) {
      throw new DuplicateResourceException("A patient with this email already exists");
    }
    var patient = new Patient(request.firstName(), request.lastName(), request.dateOfBirth(),
        request.gender(), request.email(), request.phone(), request.bloodGroup(), request.address());
    return mapper.toResponse(repository.save(patient));
  }

  @Override
  @Transactional(readOnly = true)
  public PatientResponse findById(UUID patientId) {
    return mapper.toResponse(load(patientId));
  }

  @Override
  @Transactional
  public PatientResponse update(UUID patientId, UpdatePatientRequest request) {
    if (request.email() != null && repository.existsByEmailAndIdNot(request.email(), patientId)) {
      throw new DuplicateResourceException("A patient with this email already exists");
    }
    var patient = load(patientId);
    patient.update(request.firstName(), request.lastName(), request.dateOfBirth(),
        request.gender(), request.email(), request.phone(), request.bloodGroup(),
        request.address(), request.status());
    return mapper.toResponse(patient);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<PatientResponse> search(String query, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var normalized = (query == null || query.isBlank()) ? null : query.trim();
    return PageResponse.from(repository.search(normalized, pageable).map(mapper::toResponse));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PatientSummary> summariesByIds(Collection<UUID> patientIds) {
    return repository.findAllById(patientIds).stream().map(mapper::toSummary).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long count() {
    return repository.count();
  }

  private Patient load(UUID patientId) {
    return repository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
  }
}
