package com.medflow.modules.tenancy.application;

import com.medflow.modules.tenancy.api.EntitlementStatus;
import com.medflow.modules.tenancy.api.ModuleCatalogService;
import com.medflow.modules.tenancy.api.response.ModuleEntitlementResponse;
import com.medflow.modules.tenancy.domain.entity.ModuleEntitlement;
import com.medflow.modules.tenancy.domain.repository.ModuleEntitlementRepository;
import com.medflow.modules.tenancy.domain.repository.ProductModuleRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ModuleCatalogServiceImpl implements ModuleCatalogService {

  private final ProductModuleRepository moduleRepository;
  private final ModuleEntitlementRepository entitlementRepository;
  private final Clock clock;

  ModuleCatalogServiceImpl(ProductModuleRepository moduleRepository,
      ModuleEntitlementRepository entitlementRepository, Clock clock) {
    this.moduleRepository = moduleRepository;
    this.entitlementRepository = entitlementRepository;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ModuleEntitlementResponse> forHospital(Long hospitalId) {
    var now = Instant.now(clock);
    Map<Integer, ModuleEntitlement> entitlements = entitlementRepository.findByHospitalId(hospitalId)
        .stream().collect(Collectors.toMap(ModuleEntitlement::getModuleId, Function.identity()));

    return moduleRepository.findAllByOrderByPhaseAscIdAsc().stream().map(module -> {
      var entitlement = entitlements.get(module.getId());
      var status = entitlement == null ? EntitlementStatus.DISABLED : entitlement.getStatus();
      var accessible = module.isActive() && entitlement != null && entitlement.isAccessible(now);
      return new ModuleEntitlementResponse(module.getId(), module.getModuleCode(),
          module.getModuleName(), module.getPhase(), module.getDescription(), module.isActive(),
          status, accessible,
          entitlement == null ? null : entitlement.getActivatedAt(),
          entitlement == null ? null : entitlement.getExpiresAt());
    }).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isEntitled(Long hospitalId, String moduleCode) {
    return moduleRepository.findByModuleCode(moduleCode)
        .filter(module -> module.isActive())
        .flatMap(module -> entitlementRepository.findByHospitalIdAndModuleId(hospitalId, module.getId()))
        .map(entitlement -> entitlement.isAccessible(Instant.now(clock)))
        .orElse(false);
  }
}
