package com.medflow.modules.patients.domain.repository;

import com.medflow.modules.patients.domain.entity.Patient;
import com.medflow.shared.domain.AccountStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {

  Optional<Patient> findByIdAndHospitalIdAndDeletedFalse(Long id, Long hospitalId);

  List<Patient> findByHospitalIdAndIdIn(Long hospitalId, Iterable<Long> ids);

  boolean existsByHospitalIdAndEmailIgnoreCase(Long hospitalId, String email);

  boolean existsByHospitalIdAndEmailIgnoreCaseAndIdNot(Long hospitalId, String email, Long id);

  long countByHospitalIdAndDeletedFalse(Long hospitalId);

  @Query("""
      SELECT p FROM Patient p
      WHERE p.hospitalId = :hospitalId
        AND p.deleted = FALSE
        AND (:status IS NULL OR p.status = :status)
        AND (CAST(:query AS string) IS NULL
             OR LOWER(CONCAT(p.firstName, ' ', COALESCE(p.lastName, ''))) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
             OR LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
             OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
             OR COALESCE(p.phone, '') LIKE CONCAT('%', CAST(:query AS string), '%'))
      """)
  Page<Patient> search(@Param("hospitalId") Long hospitalId, @Param("query") String query,
      @Param("status") AccountStatus status, Pageable pageable);
}
