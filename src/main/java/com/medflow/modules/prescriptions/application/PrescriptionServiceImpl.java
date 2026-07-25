package com.medflow.modules.prescriptions.application;

import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.doctors.api.DoctorSummary;
import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.modules.prescriptions.api.PrescriptionService;
import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.modules.prescriptions.api.request.CreatePrescriptionRequest;
import com.medflow.modules.prescriptions.api.response.PrescriptionResponse;
import com.medflow.modules.prescriptions.domain.entity.Prescription;
import com.medflow.modules.prescriptions.domain.repository.PrescriptionRepository;
import com.medflow.modules.prescriptions.domain.repository.PrescriptionSpecifications;
import com.medflow.modules.prescriptions.mapper.PrescriptionMapper;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PrescriptionServiceImpl implements PrescriptionService {

  private static final int MAX_PAGE_SIZE = 100;

  private final PrescriptionRepository repository;
  private final PrescriptionMapper mapper;
  private final PatientService patientService;
  private final DoctorService doctorService;

  PrescriptionServiceImpl(PrescriptionRepository repository, PrescriptionMapper mapper,
      PatientService patientService, DoctorService doctorService) {
    this.repository = repository;
    this.mapper = mapper;
    this.patientService = patientService;
    this.doctorService = doctorService;
  }

  @Override
  @Transactional
  public PrescriptionResponse create(CreatePrescriptionRequest request) {
    var patient = requirePatient(request.patientId());
    var doctor = requireDoctor(request.doctorId());

    var prescription = new Prescription(patient.id(), doctor.id(), request.notes());
    request.items().forEach(item -> prescription.addItem(item.medicationName(), item.dosage(),
        item.frequency(), item.durationDays(), item.instructions()));
    repository.save(prescription);

    return mapper.toResponse(prescription, patient.fullName(), doctor.fullName());
  }

  @Override
  @Transactional(readOnly = true)
  public PrescriptionResponse findById(UUID prescriptionId) {
    return enrich(List.of(load(prescriptionId))).getFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<PrescriptionResponse> search(UUID patientId, UUID doctorId,
      PrescriptionStatus status, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "issuedAt"));
    var result = repository.findAll(
        PrescriptionSpecifications.withFilters(patientId, doctorId, status), pageable);
    return new PageResponse<>(enrich(result.getContent()), result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages());
  }

  @Override
  @Transactional
  public PrescriptionResponse complete(UUID prescriptionId) {
    var prescription = load(prescriptionId);
    prescription.complete();
    return enrich(List.of(prescription)).getFirst();
  }

  @Override
  @Transactional
  public PrescriptionResponse cancel(UUID prescriptionId) {
    var prescription = load(prescriptionId);
    prescription.cancel();
    return enrich(List.of(prescription)).getFirst();
  }

  private List<PrescriptionResponse> enrich(List<Prescription> prescriptions) {
    var patientNames = patientService.summariesByIds(
            prescriptions.stream().map(Prescription::getPatientId).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(PatientSummary::id, PatientSummary::fullName));
    var doctorNames = doctorService.summariesByIds(
            prescriptions.stream().map(Prescription::getDoctorId).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(DoctorSummary::id, DoctorSummary::fullName));
    return prescriptions.stream()
        .map(prescription -> mapper.toResponse(prescription,
            patientNames.getOrDefault(prescription.getPatientId(), "Unknown patient"),
            doctorNames.getOrDefault(prescription.getDoctorId(), "Unknown doctor")))
        .toList();
  }

  private PatientSummary requirePatient(UUID patientId) {
    return patientService.summariesByIds(List.of(patientId)).stream().findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
  }

  private DoctorSummary requireDoctor(UUID doctorId) {
    return doctorService.summariesByIds(List.of(doctorId)).stream().findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
  }

  private Prescription load(UUID prescriptionId) {
    return repository.findById(prescriptionId)
        .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + prescriptionId));
  }
}
