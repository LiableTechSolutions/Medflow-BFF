package com.medflow.modules.patients.application;
import com.medflow.modules.patients.api.*; import com.medflow.modules.patients.api.request.CreatePatientRequest; import com.medflow.modules.patients.api.response.PatientResponse; import com.medflow.modules.patients.domain.entity.Patient; import com.medflow.modules.patients.domain.repository.PatientRepository; import com.medflow.modules.patients.mapper.PatientMapper; import com.medflow.shared.exception.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.UUID;
@Service class PatientServiceImpl implements PatientService {
  private final PatientRepository repository; private final PatientMapper mapper;
  PatientServiceImpl(PatientRepository repository, PatientMapper mapper) { this.repository=repository; this.mapper=mapper; }
  @Override @Transactional public PatientResponse create(CreatePatientRequest request) { if (request.email()!=null && repository.existsByEmail(request.email())) throw new DuplicateResourceException("A patient with this email already exists"); return mapper.toResponse(repository.save(new Patient(request.firstName(), request.lastName(), request.dateOfBirth(), request.gender(), request.email()))); }
  @Override @Transactional(readOnly=true) public PatientResponse findById(UUID id) { return mapper.toResponse(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id))); }
}
