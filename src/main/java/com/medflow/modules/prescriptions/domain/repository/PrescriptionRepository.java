package com.medflow.modules.prescriptions.domain.repository;

import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.modules.prescriptions.domain.entity.Prescription;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrescriptionRepository
    extends JpaRepository<Prescription, Long>, JpaSpecificationExecutor<Prescription> {

  Optional<Prescription> findByIdAndHospitalId(Long id, Long hospitalId);

  long countByHospitalId(Long hospitalId);

  /** Cross-tenant on purpose: the daily follow-up reminder job runs outside any request. */
  List<Prescription> findByFollowUpDateAndStatus(LocalDate followUpDate, PrescriptionStatus status);
}
