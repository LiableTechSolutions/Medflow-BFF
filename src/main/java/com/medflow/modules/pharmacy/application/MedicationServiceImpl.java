package com.medflow.modules.pharmacy.application;

import com.medflow.modules.pharmacy.api.MedicationLowStockEvent;
import com.medflow.modules.pharmacy.api.MedicationService;
import com.medflow.modules.pharmacy.api.request.AdjustStockRequest;
import com.medflow.modules.pharmacy.api.request.CreateMedicationRequest;
import com.medflow.modules.pharmacy.api.request.UpdateMedicationRequest;
import com.medflow.modules.pharmacy.api.response.MedicationResponse;
import com.medflow.modules.pharmacy.domain.entity.Medication;
import com.medflow.modules.pharmacy.domain.repository.MedicationRepository;
import com.medflow.modules.pharmacy.mapper.MedicationMapper;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.exception.DuplicateResourceException;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class MedicationServiceImpl implements MedicationService {

  private static final int MAX_PAGE_SIZE = 100;

  private final MedicationRepository repository;
  private final MedicationMapper mapper;
  private final ApplicationEventPublisher eventPublisher;

  MedicationServiceImpl(MedicationRepository repository, MedicationMapper mapper,
      ApplicationEventPublisher eventPublisher) {
    this.repository = repository;
    this.mapper = mapper;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public MedicationResponse create(CreateMedicationRequest request) {
    if (repository.existsByNameIgnoreCase(request.name())) {
      throw new DuplicateResourceException("A medication with this name already exists");
    }
    var medication = new Medication(request.name(), request.category(), request.unitPrice(),
        request.stockQuantity(), request.reorderLevel(), request.expiryDate());
    return mapper.toResponse(repository.save(medication));
  }

  @Override
  @Transactional(readOnly = true)
  public MedicationResponse findById(UUID medicationId) {
    return mapper.toResponse(load(medicationId));
  }

  @Override
  @Transactional
  public MedicationResponse update(UUID medicationId, UpdateMedicationRequest request) {
    if (repository.existsByNameIgnoreCaseAndIdNot(request.name(), medicationId)) {
      throw new DuplicateResourceException("A medication with this name already exists");
    }
    var medication = load(medicationId);
    medication.update(request.name(), request.category(), request.unitPrice(),
        request.reorderLevel(), request.expiryDate());
    return mapper.toResponse(medication);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<MedicationResponse> search(String query, boolean lowStockOnly, int page,
      int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.ASC, "name"));
    var normalized = (query == null || query.isBlank()) ? null : query.trim();
    return PageResponse.from(
        repository.search(normalized, lowStockOnly, pageable).map(mapper::toResponse));
  }

  @Override
  @Transactional
  public MedicationResponse adjustStock(UUID medicationId, AdjustStockRequest request) {
    var medication = load(medicationId);
    var crossedThreshold = medication.adjustStock(request.delta());
    if (crossedThreshold) {
      eventPublisher.publishEvent(new MedicationLowStockEvent(medication.getId(),
          medication.getName(), medication.getStockQuantity(), medication.getReorderLevel()));
    }
    return mapper.toResponse(medication);
  }

  private Medication load(UUID medicationId) {
    return repository.findById(medicationId)
        .orElseThrow(() -> new ResourceNotFoundException("Medication not found: " + medicationId));
  }
}
