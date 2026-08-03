package com.medflow.modules.prescriptions.domain.repository;

import com.medflow.modules.prescriptions.domain.entity.Prescription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrescriptionRepository
    extends JpaRepository<Prescription, Long>, JpaSpecificationExecutor<Prescription> {

  Optional<Prescription> findByIdAndHospitalId(Long id, Long hospitalId);

  long countByHospitalId(Long hospitalId);
}
