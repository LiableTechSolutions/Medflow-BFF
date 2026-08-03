package com.medflow.modules.patients.domain.repository;

import com.medflow.modules.patients.domain.entity.UserPatientMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPatientMappingRepository extends JpaRepository<UserPatientMapping, Long> {

  List<UserPatientMapping> findByPatientId(Long patientId);

  List<UserPatientMapping> findByUserId(Long userId);

  boolean existsByUserIdAndPatientId(Long userId, Long patientId);
}
