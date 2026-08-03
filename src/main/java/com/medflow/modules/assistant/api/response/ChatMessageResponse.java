package com.medflow.modules.assistant.api.response;

import com.medflow.modules.assistant.api.ChatRole;
import java.time.Instant;

public record ChatMessageResponse(
    Long id,
    String conversationId,
    ChatRole role,
    String content,
    Instant createdAt) {
}
