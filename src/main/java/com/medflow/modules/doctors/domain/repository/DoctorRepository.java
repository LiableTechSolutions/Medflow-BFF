package com.medflow.modules.doctors.domain.repository;

import com.medflow.modules.doctors.domain.entity.Doctor;
import com.medflow.shared.domain.AccountStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

  Optional<Doctor> findByIdAndHospitalId(Long id, Long hospitalId);

  List<Doctor> findByHospitalIdAndIdIn(Long hospitalId, Iterable<Long> ids);

  boolean existsByRegistrationNumber(String registrationNumber);

  long countByHospitalId(Long hospitalId);

  /**
   * Free-text search over the doctor's own columns. Name matching happens in the users
   * module, which passes the matching user ids in.
   */
  @Query("""
      SELECT d FROM Doctor d
      WHERE d.hospitalId = :hospitalId
        AND (CAST(:specialty AS string) IS NULL OR LOWER(d.specialty) = LOWER(CAST(:specialty AS string)))
        AND (:status IS NULL OR d.status = :status)
        AND (CAST(:query AS string) IS NULL
             OR LOWER(d.specialty) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
             OR LOWER(d.doctorCode) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
             OR d.userId IN :matchingUserIds)
      """)
  Page<Doctor> search(@Param("hospitalId") Long hospitalId, @Param("query") String query,
      @Param("specialty") String specialty, @Param("status") AccountStatus status,
      @Param("matchingUserIds") List<Long> matchingUserIds, Pageable pageable);
}
