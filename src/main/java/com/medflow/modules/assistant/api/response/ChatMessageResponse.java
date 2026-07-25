package com.medflow.modules.assistant.api.response;

import com.medflow.modules.assistant.api.ChatRole;
import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
    UUID id,
    UUID conversationId,
    ChatRole role,
    String content,
    Instant createdAt) {
}
