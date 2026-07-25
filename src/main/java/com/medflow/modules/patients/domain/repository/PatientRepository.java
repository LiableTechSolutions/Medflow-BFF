package com.medflow.modules.patients.domain.repository;

import com.medflow.modules.patients.domain.entity.Patient;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, UUID id);

  @Query("""
      SELECT p FROM Patient p
      WHERE :query IS NULL
         OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :query, '%'))
         OR LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%'))
      """)
  Page<Patient> search(@Param("query") String query, Pageable pageable);
}
