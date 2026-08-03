package com.medflow.modules.audit.domain.repository;

import com.medflow.modules.audit.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  @Query("""
      SELECT a FROM AuditLog a
      WHERE a.hospitalId = :hospitalId
        AND (:entityType IS NULL OR a.entityType = :entityType)
        AND (:userId IS NULL OR a.userId = :userId)
      """)
  Page<AuditLog> search(@Param("hospitalId") Long hospitalId,
      @Param("entityType") String entityType, @Param("userId") Long userId, Pageable pageable);
}
