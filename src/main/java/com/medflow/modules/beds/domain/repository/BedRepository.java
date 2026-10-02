package com.medflow.modules.beds.domain.repository;

import com.medflow.modules.beds.api.BedStatus;
import com.medflow.modules.beds.domain.entity.Bed;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BedRepository extends JpaRepository<Bed, Long> {

  Optional<Bed> findByIdAndHospitalId(Long id, Long hospitalId);

  List<Bed> findByWardIdAndHospitalIdOrderByBedNumberAsc(Long wardId, Long hospitalId);

  List<Bed> findByHospitalId(Long hospitalId);

  /** Free beds, highest number first — the ones a reduction gives up. */
  List<Bed> findByWardIdAndStatusOrderByBedNumberDesc(Long wardId, BedStatus status);

  boolean existsByPatientId(Long patientId);

  Optional<Bed> findByPatientIdAndHospitalId(Long patientId, Long hospitalId);

  List<Bed> findByHospitalIdAndStatus(Long hospitalId, BedStatus status);

  @Query("SELECT COALESCE(MAX(b.bedNumber), 0) FROM Bed b WHERE b.wardId = :wardId")
  int highestBedNumber(@Param("wardId") Long wardId);
}
