package com.medflow.modules.pharmacy.domain.repository;

import com.medflow.modules.pharmacy.domain.entity.Medication;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicationRepository extends JpaRepository<Medication, UUID> {

  boolean existsByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

  @Query("""
      SELECT m FROM Medication m
      WHERE (:query IS NULL
             OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(m.category) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:lowStockOnly = FALSE OR m.stockQuantity <= m.reorderLevel)
      """)
  Page<Medication> search(@Param("query") String query,
      @Param("lowStockOnly") boolean lowStockOnly, Pageable pageable);
}
