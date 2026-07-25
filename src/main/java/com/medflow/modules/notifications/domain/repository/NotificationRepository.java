package com.medflow.modules.notifications.domain.repository;

import com.medflow.modules.notifications.domain.entity.Notification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  Page<Notification> findByReadFalse(Pageable pageable);

  long countByReadFalse();

  @Modifying
  @Query("UPDATE Notification n SET n.read = TRUE WHERE n.read = FALSE")
  int markAllRead();
}
