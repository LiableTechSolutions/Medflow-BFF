package com.medflow.modules.tenancy.domain.repository;

import com.medflow.modules.tenancy.domain.entity.Hospital;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

  Optional<Hospital> findByIdAndDeletedFalse(Long id);

  Optional<Hospital> findByHospitalCodeAndDeletedFalse(String hospitalCode);

  boolean existsByHospitalCode(String hospitalCode);
}
