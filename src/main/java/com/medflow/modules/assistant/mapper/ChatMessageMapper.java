package com.medflow.modules.assistant.mapper;

import com.medflow.modules.assistant.api.response.ChatMessageResponse;
import com.medflow.modules.assistant.domain.entity.ChatMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

  ChatMessageResponse toResponse(ChatMessage message);
}
