package com.medflow.modules.laboratory.application;

import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.doctors.api.DoctorSummary;
import com.medflow.modules.laboratory.api.LabOrderCompletedEvent;
import com.medflow.modules.laboratory.api.LabOrderService;
import com.medflow.modules.laboratory.api.LabOrderStatus;
import com.medflow.modules.laboratory.api.LabPriority;
import com.medflow.modules.laboratory.api.request.CompleteLabOrderRequest;
import com.medflow.modules.laboratory.api.request.CreateLabOrderRequest;
import com.medflow.modules.laboratory.api.response.LabOrderResponse;
import com.medflow.modules.laboratory.domain.entity.LabOrder;
import com.medflow.modules.laboratory.domain.repository.LabOrderRepository;
import com.medflow.modules.laboratory.domain.repository.LabOrderSpecifications;
import com.medflow.modules.laboratory.mapper.LabOrderMapper;
import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class LabOrderServiceImpl implements LabOrderService {

  private static final int MAX_PAGE_SIZE = 100;

  private final LabOrderRepository repository;
  private final LabOrderMapper mapper;
  private final PatientService patientService;
  private final DoctorService doctorService;
  private final ApplicationEventPublisher eventPublisher;

  LabOrderServiceImpl(LabOrderRepository repository, LabOrderMapper mapper,
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
  public LabOrderResponse create(CreateLabOrderRequest request) {
    var patient = requirePatient(request.patientId());
    var doctor = requireDoctor(request.orderedBy());
    var labOrder = new LabOrder(patient.id(), doctor.id(), request.testName(), request.priority());
    repository.save(labOrder);
    return mapper.toResponse(labOrder, patient.fullName(), doctor.fullName());
  }

  @Override
  @Transactional(readOnly = true)
  public LabOrderResponse findById(UUID labOrderId) {
    return enrich(List.of(load(labOrderId))).getFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<LabOrderResponse> search(LabOrderStatus status, LabPriority priority,
      UUID patientId, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "orderedAt"));
    var result = repository.findAll(
        LabOrderSpecifications.withFilters(status, priority, patientId), pageable);
    return new PageResponse<>(enrich(result.getContent()), result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages());
  }

  @Override
  @Transactional
  public LabOrderResponse startProcessing(UUID labOrderId) {
    var labOrder = load(labOrderId);
    labOrder.startProcessing();
    return enrich(List.of(labOrder)).getFirst();
  }

  @Override
  @Transactional
  public LabOrderResponse complete(UUID labOrderId, CompleteLabOrderRequest request) {
    var labOrder = load(labOrderId);
    labOrder.complete(request.resultSummary());
    var patientName = patientService.summariesByIds(List.of(labOrder.getPatientId())).stream()
        .findFirst().map(PatientSummary::fullName).orElse("Unknown patient");
    eventPublisher.publishEvent(new LabOrderCompletedEvent(labOrder.getId(),
        labOrder.getPatientId(), patientName, labOrder.getTestName()));
    return enrich(List.of(labOrder)).getFirst();
  }

  @Override
  @Transactional
  public LabOrderResponse cancel(UUID labOrderId) {
    var labOrder = load(labOrderId);
    labOrder.cancel();
    return enrich(List.of(labOrder)).getFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public long countCompleted() {
    return repository.countByStatus(LabOrderStatus.COMPLETED);
  }

  private List<LabOrderResponse> enrich(List<LabOrder> labOrders) {
    var patientNames = patientService.summariesByIds(
            labOrders.stream().map(LabOrder::getPatientId).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(PatientSummary::id, PatientSummary::fullName));
    var doctorNames = doctorService.summariesByIds(
            labOrders.stream().map(LabOrder::getOrderedBy).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(DoctorSummary::id, DoctorSummary::fullName));
    return labOrders.stream()
        .map(labOrder -> mapper.toResponse(labOrder,
            patientNames.getOrDefault(labOrder.getPatientId(), "Unknown patient"),
            doctorNames.getOrDefault(labOrder.getOrderedBy(), "Unknown doctor")))
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

  private LabOrder load(UUID labOrderId) {
    return repository.findById(labOrderId)
        .orElseThrow(() -> new ResourceNotFoundException("Lab order not found: " + labOrderId));
  }
}
