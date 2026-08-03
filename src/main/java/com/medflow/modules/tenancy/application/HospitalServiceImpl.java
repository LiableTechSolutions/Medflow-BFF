package com.medflow.modules.tenancy.application;

import com.medflow.modules.tenancy.api.EntitlementStatus;
import com.medflow.modules.tenancy.api.HospitalService;
import com.medflow.modules.tenancy.api.HospitalSummary;
import com.medflow.modules.tenancy.api.request.RegisterHospitalRequest;
import com.medflow.modules.tenancy.api.request.UpdateHospitalRequest;
import com.medflow.modules.tenancy.api.response.HospitalResponse;
import com.medflow.modules.tenancy.domain.entity.Hospital;
import com.medflow.modules.tenancy.domain.entity.ModuleEntitlement;
import com.medflow.modules.tenancy.domain.repository.HospitalRepository;
import com.medflow.modules.tenancy.domain.repository.ModuleEntitlementRepository;
import com.medflow.modules.tenancy.domain.repository.ProductModuleRepository;
import com.medflow.modules.tenancy.mapper.HospitalMapper;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class HospitalServiceImpl implements HospitalService {

  private static final String DEFAULT_TIMEZONE = "Asia/Kolkata";

  private final HospitalRepository repository;
  private final ProductModuleRepository moduleRepository;
  private final ModuleEntitlementRepository entitlementRepository;
  private final HospitalMapper mapper;

  HospitalServiceImpl(HospitalRepository repository, ProductModuleRepository moduleRepository,
      ModuleEntitlementRepository entitlementRepository, HospitalMapper mapper) {
    this.repository = repository;
    this.moduleRepository = moduleRepository;
    this.entitlementRepository = entitlementRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public HospitalResponse register(RegisterHospitalRequest request) {
    var timezone = (request.timezone() == null || request.timezone().isBlank())
        ? DEFAULT_TIMEZONE
        : request.timezone();
    var hospital = repository.save(new Hospital(nextHospitalCode(request.name()), request.name(),
        request.legalName(), request.hospitalType(), request.addressLine1(), request.city(),
        request.state(), request.country(), request.pincode(), request.phone(), request.email(),
        timezone));
    entitleGenerallyAvailableModules(hospital.getId());
    return mapper.toResponse(hospital);
  }

  @Override
  @Transactional(readOnly = true)
  public HospitalResponse findById(Long hospitalId) {
    return mapper.toResponse(load(hospitalId));
  }

  @Override
  @Transactional
  public HospitalResponse update(Long hospitalId, UpdateHospitalRequest request) {
    var hospital = load(hospitalId);
    hospital.update(request);
    return mapper.toResponse(hospital);
  }

  @Override
  @Transactional(readOnly = true)
  public HospitalSummary summary(Long hospitalId) {
    return mapper.toSummary(load(hospitalId));
  }

  /** A new workspace gets every generally available (phase 1) module switched on. */
  private void entitleGenerallyAvailableModules(Long hospitalId) {
    moduleRepository.findAllByOrderByPhaseAscIdAsc().forEach(module -> {
      var status = module.isActive() && module.getPhase() == 1
          ? EntitlementStatus.ACTIVE
          : EntitlementStatus.DISABLED;
      entitlementRepository.save(new ModuleEntitlement(hospitalId, module.getId(), status));
    });
  }

  /**
   * Derives a readable, unique tenant code from the hospital name — "City Care Hospital"
   * becomes CITYCARE-01, CITYCARE-02 and so on.
   */
  private String nextHospitalCode(String name) {
    var prefix = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    prefix = prefix.isEmpty() ? "HOSP" : prefix.substring(0, Math.min(8, prefix.length()));
    for (var suffix = 1; suffix < 100; suffix++) {
      var candidate = "%s-%02d".formatted(prefix, suffix);
      if (!repository.existsByHospitalCode(candidate)) {
        return candidate;
      }
    }
    return "%s-%d".formatted(prefix, System.currentTimeMillis() % 100000);
  }

  private Hospital load(Long hospitalId) {
    return repository.findByIdAndDeletedFalse(hospitalId)
        .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + hospitalId));
  }
}
