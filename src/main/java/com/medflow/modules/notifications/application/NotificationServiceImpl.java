package com.medflow.modules.notifications.application;

import com.medflow.modules.notifications.api.NotificationService;
import com.medflow.modules.notifications.api.response.NotificationResponse;
import com.medflow.modules.notifications.domain.repository.NotificationRepository;
import com.medflow.modules.notifications.mapper.NotificationMapper;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class NotificationServiceImpl implements NotificationService {

  private static final int MAX_PAGE_SIZE = 100;

  private final NotificationRepository repository;
  private final NotificationMapper mapper;

  NotificationServiceImpl(NotificationRepository repository, NotificationMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<NotificationResponse> list(boolean unreadOnly, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var result = unreadOnly ? repository.findByReadFalse(pageable) : repository.findAll(pageable);
    return PageResponse.from(result.map(mapper::toResponse));
  }

  @Override
  @Transactional(readOnly = true)
  public long unreadCount() {
    return repository.countByReadFalse();
  }

  @Override
  @Transactional
  public NotificationResponse markRead(UUID notificationId) {
    var notification = repository.findById(notificationId)
        .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
    notification.markRead();
    return mapper.toResponse(notification);
  }

  @Override
  @Transactional
  public int markAllRead() {
    return repository.markAllRead();
  }
}
