package com.medflow.modules.notifications.domain.repository;

import com.medflow.modules.notifications.domain.entity.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Optional<Notification> findByIdAndHospitalId(Long id, Long hospitalId);

  Page<Notification> findByHospitalId(Long hospitalId, Pageable pageable);

  Page<Notification> findByHospitalIdAndReadFalse(Long hospitalId, Pageable pageable);

  long countByHospitalIdAndReadFalse(Long hospitalId);

  @Modifying
  @Query("UPDATE Notification n SET n.read = TRUE WHERE n.hospitalId = :hospitalId AND n.read = FALSE")
  int markAllRead(@Param("hospitalId") Long hospitalId);
}
