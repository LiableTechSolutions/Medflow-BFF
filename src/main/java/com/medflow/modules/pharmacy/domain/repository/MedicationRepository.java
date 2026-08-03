package com.medflow.modules.pharmacy.domain.repository;

import com.medflow.modules.pharmacy.domain.entity.Medication;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

  Optional<Medication> findByIdAndHospitalId(Long id, Long hospitalId);

  boolean existsByHospitalIdAndNameIgnoreCase(Long hospitalId, String name);

  boolean existsByHospitalIdAndNameIgnoreCaseAndIdNot(Long hospitalId, String name, Long id);

  @Query("""
      SELECT m FROM Medication m
      WHERE m.hospitalId = :hospitalId
        AND (CAST(:query AS string) IS NULL
             OR LOWER(m.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
             OR LOWER(m.category) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
        AND (:lowStockOnly = FALSE OR m.stockQuantity <= m.reorderLevel)
      """)
  Page<Medication> search(@Param("hospitalId") Long hospitalId, @Param("query") String query,
      @Param("lowStockOnly") boolean lowStockOnly, Pageable pageable);
}
