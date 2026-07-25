package com.medflow.modules.notifications.api;

import com.medflow.modules.notifications.api.response.NotificationResponse;
import com.medflow.shared.api.PageResponse;
import java.util.UUID;

/** Public API of the Notifications module. */
public interface NotificationService {

  PageResponse<NotificationResponse> list(boolean unreadOnly, int page, int size);

  /** Powers the sidebar badge. */
  long unreadCount();

  NotificationResponse markRead(UUID notificationId);

  int markAllRead();
}
