/**
 * Notifications module: the workspace-wide alert feed. Subscribes to domain events from
 * appointments, laboratory and pharmacy — never calls those modules directly — and
 * exposes read/unread management for the notification bell.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Notifications")
package com.medflow.modules.notifications;
