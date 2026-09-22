package com.medflow.modules.notifications.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends the booking notification by email — or logs what would have been sent, when no
 * SMTP password is configured yet (an empty {@code spring.mail.password} means no
 * account has been set up). {@link JavaMailSender} is always present once
 * spring-boot-starter-mail is on the classpath and {@code spring.mail.host} has a
 * default value, regardless of whether real credentials exist.
 */
@Component
class EmailChannelSender {

  private static final Logger log = LoggerFactory.getLogger(EmailChannelSender.class);

  private final JavaMailSender mailSender;
  private final NotificationProperties properties;
  private final String smtpPassword;

  EmailChannelSender(JavaMailSender mailSender, NotificationProperties properties,
      @Value("${spring.mail.password:}") String smtpPassword) {
    this.mailSender = mailSender;
    this.properties = properties;
    this.smtpPassword = smtpPassword;
  }

  void send(String toEmail, String subject, String body) {
    if (toEmail == null || toEmail.isBlank()) {
      log.info("[email-mock] patient has no email on file — would have sent \"{}\": {}", subject, body);
      return;
    }
    if (smtpPassword.isBlank()) {
      log.info("[email-mock] would send to {} \"{}\": {}", toEmail, subject, body);
      return;
    }
    var message = new SimpleMailMessage();
    message.setFrom(properties.fromEmail());
    message.setTo(toEmail);
    message.setSubject(subject);
    message.setText(body);
    try {
      mailSender.send(message);
    } catch (MailException e) {
      log.warn("Failed to send email to {}", toEmail, e);
    }
  }
}
