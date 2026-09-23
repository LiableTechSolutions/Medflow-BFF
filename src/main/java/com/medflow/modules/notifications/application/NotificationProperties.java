package com.medflow.modules.notifications.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed access to the {@code medflow.notifications.*} configuration tree. */
@ConfigurationProperties(prefix = "medflow.notifications")
record NotificationProperties(String frontendBaseUrl, String fromEmail, Twilio twilio) {

  /**
   * Empty strings (the default when the env vars aren't set) mean "no account yet" —
   * senders check {@link #configured()} and log a mock line instead of calling out.
   */
  record Twilio(String accountSid, String authToken, String whatsappFrom, String smsFrom,
      String whatsappContentSid) {

    boolean configured() {
      return notBlank(accountSid) && notBlank(authToken);
    }

    private static boolean notBlank(String value) {
      return value != null && !value.isBlank();
    }
  }
}
