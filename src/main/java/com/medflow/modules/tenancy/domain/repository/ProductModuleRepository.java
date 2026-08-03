package com.medflow.modules.tenancy.domain.repository;

import com.medflow.modules.tenancy.domain.entity.ProductModule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductModuleRepository extends JpaRepository<ProductModule, Integer> {

  List<ProductModule> findAllByOrderByPhaseAscIdAsc();

  List<ProductModule> findByActiveTrue();

  Optional<ProductModule> findByModuleCode(String moduleCode);
}
