package com.medflow.modules.assistant.api;

import com.medflow.modules.assistant.api.request.SendMessageRequest;
import com.medflow.modules.assistant.api.response.ChatMessageResponse;
import com.medflow.shared.api.PageResponse;
import java.util.UUID;

/** Public API of the Assistant module. */
public interface AssistantService {

  /**
   * Stores the user's message, generates a reply and returns it. Omitting
   * {@code conversationId} starts a new conversation.
   */
  ChatMessageResponse send(UUID userId, SendMessageRequest request);

  PageResponse<ChatMessageResponse> history(UUID userId, UUID conversationId, int page, int size);
}
