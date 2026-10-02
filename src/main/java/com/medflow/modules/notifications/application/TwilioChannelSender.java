package com.medflow.modules.notifications.application;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Sends the booking notification by WhatsApp and SMS via Twilio — or logs what would
 * have been sent, when no account is configured yet (see
 * {@link NotificationProperties.Twilio#configured()}). Both channels share one Twilio
 * account; only the sender number and the {@code whatsapp:} prefix differ.
 */
@Component
class TwilioChannelSender {

  private static final Logger log = LoggerFactory.getLogger(TwilioChannelSender.class);

  private final NotificationProperties.Twilio properties;
  private boolean initialized;

  TwilioChannelSender(NotificationProperties properties) {
    this.properties = properties.twilio();
  }

  void sendWhatsApp(String toPhone, String message) {
    send("whatsapp", properties.whatsappFrom(), "whatsapp:", toPhone, message, properties.whatsappContentSid());
  }

  void sendSms(String toPhone, String message) {
    send("sms", properties.smsFrom(), "", toPhone, message, null);
  }

  private void send(String channel, String from, String toPrefix, String toPhone, String message,
      String contentSid) {
    if (toPhone == null || toPhone.isBlank()) {
      log.info("[{}-mock] patient has no phone on file — would have sent: {}", channel, message);
      return;
    }
    if (!properties.configured() || from == null || from.isBlank()) {
      log.info("[{}-mock] would send to {}: {}", channel, toPhone, message);
      return;
    }
    ensureInitialized();
    var useTemplate = contentSid != null && !contentSid.isBlank();
    try {
      var creator = Message.creator(new PhoneNumber(toPrefix + toE164(toPhone)), new PhoneNumber(from),
          useTemplate ? null : message);
      if (useTemplate) {
        // Trial WhatsApp senders can only use an approved Content Template, not
        // freeform text. This template's copy is a fixed Twilio sample — it does not
        // carry the real patient/doctor/time from `message`. Drop TWILIO_WHATSAPP_CONTENT_SID
        // once the account is upgraded so real appointment details go out instead.
        creator.setContentSid(contentSid);
      }
      creator.create();
    } catch (RuntimeException e) {
      log.warn("Failed to send {} to {}", channel, toPhone, e);
    }
  }

  private synchronized void ensureInitialized() {
    if (!initialized) {
      com.twilio.Twilio.init(properties.accountSid(), properties.authToken());
      initialized = true;
    }
  }

  /** The rest of the app stores a bare 10-digit Indian mobile number, no country code. */
  private static String toE164(String phone) {
    var digits = phone.replaceAll("[^0-9+]", "");
    if (digits.startsWith("+")) return digits;
    return digits.length() == 10 ? "+91" + digits : "+" + digits;
  }
}
