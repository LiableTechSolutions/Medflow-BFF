package com.medflow.modules.notifications.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed access to the {@code medflow.notifications.*} configuration tree. */
@ConfigurationProperties(prefix = "medflow.notifications")
record NotificationProperties(String frontendBaseUrl) {
}
