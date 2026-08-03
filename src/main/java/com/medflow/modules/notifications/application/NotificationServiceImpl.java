package com.medflow.modules.notifications.application;

import com.medflow.modules.notifications.api.NotificationService;
import com.medflow.modules.notifications.api.response.NotificationResponse;
import com.medflow.modules.notifications.domain.entity.Notification;
import com.medflow.modules.notifications.domain.repository.NotificationRepository;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class NotificationServiceImpl implements NotificationService {

  private static final int MAX_PAGE_SIZE = 100;

  private final NotificationRepository repository;

  NotificationServiceImpl(NotificationRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<NotificationResponse> list(Long hospitalId, boolean unreadOnly, int page,
      int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var result = unreadOnly
        ? repository.findByHospitalIdAndReadFalse(hospitalId, pageable)
        : repository.findByHospitalId(hospitalId, pageable);
    return PageResponse.from(result.map(this::toResponse));
  }

  @Override
  @Transactional(readOnly = true)
  public long unreadCount(Long hospitalId) {
    return repository.countByHospitalIdAndReadFalse(hospitalId);
  }

  @Override
  @Transactional
  public NotificationResponse markRead(Long hospitalId, Long notificationId) {
    var notification = repository.findByIdAndHospitalId(notificationId, hospitalId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Notification not found: " + notificationId));
    notification.markRead();
    return toResponse(notification);
  }

  @Override
  @Transactional
  public int markAllRead(Long hospitalId) {
    return repository.markAllRead(hospitalId);
  }

  private NotificationResponse toResponse(Notification notification) {
    return new NotificationResponse(notification.getId(), notification.getHospitalId(),
        notification.getCategory(), notification.getSeverity(), notification.getTitle(),
        notification.getMessage(), notification.isRead(), notification.getCreatedAt());
  }
}
