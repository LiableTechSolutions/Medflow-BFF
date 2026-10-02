package com.medflow.modules.beds.domain.repository;

import com.medflow.modules.beds.domain.entity.Ward;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WardRepository extends JpaRepository<Ward, Long> {

  Optional<Ward> findByIdAndHospitalId(Long id, Long hospitalId);

  List<Ward> findByHospitalIdOrderByNameAsc(Long hospitalId);

  boolean existsByHospitalIdAndNameIgnoreCase(Long hospitalId, String name);
}
