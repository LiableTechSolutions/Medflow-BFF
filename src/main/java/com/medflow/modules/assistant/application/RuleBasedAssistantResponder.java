package com.medflow.modules.assistant.application;

import com.medflow.modules.assistant.domain.AssistantResponder;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Deterministic placeholder adapter. Replace with an LLM-backed implementation of
 * {@link AssistantResponder} to enable real clinical chat; nothing else changes.
 */
@Component
class RuleBasedAssistantResponder implements AssistantResponder {

  @Override
  public String reply(String userMessage) {
    var normalized = userMessage.toLowerCase(Locale.ROOT);
    if (normalized.contains("summar")) {
      return "Summarization of visit notes will be available once an LLM provider is "
          + "configured for this workspace. Meanwhile, you can review the patient's visit "
          + "history under Patient Management.";
    }
    if (normalized.contains("appointment") || normalized.contains("schedule")) {
      return "You can review and manage today's schedule under Appointment Management. "
          + "Booking conflicts are flagged automatically in Notifications.";
    }
    if (normalized.contains("lab") || normalized.contains("result")) {
      return "Completed lab results appear in the Laboratory module, and a notification is "
          + "raised the moment results are signed off.";
    }
    return "I'm a placeholder assistant for now — clinical chat, summarization and insights "
        + "activate when an AI provider is connected. Your message has been saved to this "
        + "conversation.";
  }
}
