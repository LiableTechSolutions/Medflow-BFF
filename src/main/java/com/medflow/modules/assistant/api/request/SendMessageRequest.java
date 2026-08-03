package com.medflow.modules.assistant.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
    @NotBlank @Size(max = 4000) String content,
    @Size(max = 36) String conversationId) {
}
