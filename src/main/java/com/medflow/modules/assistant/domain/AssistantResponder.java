package com.medflow.modules.assistant.domain;

/**
 * Port for reply generation. Implementations must be side-effect free; conversation
 * persistence is handled by the application service.
 */
public interface AssistantResponder {

  String reply(String userMessage);
}
