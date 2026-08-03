package com.medflow.modules.notifications.api;

import com.medflow.modules.notifications.api.response.NotificationResponse;
import com.medflow.shared.api.PageResponse;

/** Public API of the Notifications module. */
public interface NotificationService {

  PageResponse<NotificationResponse> list(Long hospitalId, boolean unreadOnly, int page, int size);

  /** Powers the sidebar badge. */
  long unreadCount(Long hospitalId);

  NotificationResponse markRead(Long hospitalId, Long notificationId);

  int markAllRead(Long hospitalId);
}
