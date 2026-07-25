package com.medflow.modules.assistant.mapper;

import com.medflow.modules.assistant.api.ChatRole;
import com.medflow.modules.assistant.api.response.ChatMessageResponse;
import com.medflow.modules.assistant.domain.entity.ChatMessage;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T19:48:23+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ChatMessageMapperImpl implements ChatMessageMapper {

    @Override
    public ChatMessageResponse toResponse(ChatMessage message) {
        if ( message == null ) {
            return null;
        }

        UUID id = null;
        UUID conversationId = null;
        ChatRole role = null;
        String content = null;
        Instant createdAt = null;

        id = message.getId();
        conversationId = message.getConversationId();
        role = message.getRole();
        content = message.getContent();
        createdAt = message.getCreatedAt();

        ChatMessageResponse chatMessageResponse = new ChatMessageResponse( id, conversationId, role, content, createdAt );

        return chatMessageResponse;
    }
}
