package com.medflow.modules.tenancy.domain.repository;

import com.medflow.modules.tenancy.domain.entity.ModuleEntitlement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleEntitlementRepository extends JpaRepository<ModuleEntitlement, Long> {

  List<ModuleEntitlement> findByHospitalId(Long hospitalId);

  Optional<ModuleEntitlement> findByHospitalIdAndModuleId(Long hospitalId, Integer moduleId);
}
