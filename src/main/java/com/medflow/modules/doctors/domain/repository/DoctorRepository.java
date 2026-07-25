package com.medflow.modules.doctors.domain.repository;

import com.medflow.modules.doctors.api.DoctorAvailability;
import com.medflow.modules.doctors.domain.entity.Doctor;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

  boolean existsByLicenseNumber(String licenseNumber);

  @Query("""
      SELECT d FROM Doctor d
      WHERE (:query IS NULL
             OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(d.email) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:specialty IS NULL OR LOWER(d.specialty) = LOWER(:specialty))
        AND (:availability IS NULL OR d.availability = :availability)
      """)
  Page<Doctor> search(@Param("query") String query, @Param("specialty") String specialty,
      @Param("availability") DoctorAvailability availability, Pageable pageable);
}
