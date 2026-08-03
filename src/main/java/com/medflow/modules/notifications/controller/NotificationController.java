package com.medflow.modules.notifications.controller;

import com.medflow.modules.notifications.api.NotificationService;
import com.medflow.modules.notifications.api.response.NotificationResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController {

  private final NotificationService service;
  private final TenantContext tenantContext;

  NotificationController(NotificationService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @GetMapping
  @Operation(summary = "List notifications", description = "The workspace alert feed, newest first.")
  ApiResponse<PageResponse<NotificationResponse>> list(
      @RequestParam(defaultValue = "false") boolean unreadOnly,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Notifications retrieved successfully",
        service.list(tenantContext.hospitalId(), unreadOnly, page, size));
  }

  @GetMapping("/unread-count")
  @Operation(summary = "Unread count", description = "Powers the sidebar notification badge.")
  ApiResponse<Map<String, Long>> unreadCount() {
    return ApiResponse.success("Unread count retrieved successfully",
        Map.of("unread", service.unreadCount(tenantContext.hospitalId())));
  }

  @PatchMapping("/{notificationId}/read")
  @Operation(summary = "Mark as read", description = "Marks one notification as read.")
  ApiResponse<NotificationResponse> markRead(@PathVariable Long notificationId) {
    return ApiResponse.success("Notification marked as read",
        service.markRead(tenantContext.hospitalId(), notificationId));
  }

  @PatchMapping("/read-all")
  @Operation(summary = "Mark all as read", description = "Clears the unread badge.")
  ApiResponse<Map<String, Integer>> markAllRead() {
    return ApiResponse.success("All notifications marked as read",
        Map.of("updated", service.markAllRead(tenantContext.hospitalId())));
  }
}
