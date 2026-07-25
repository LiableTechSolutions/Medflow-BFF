package com.medflow.modules.assistant.domain.repository;

import com.medflow.modules.assistant.domain.entity.ChatMessage;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

  Page<ChatMessage> findByUserId(UUID userId, Pageable pageable);

  Page<ChatMessage> findByUserIdAndConversationId(UUID userId, UUID conversationId,
      Pageable pageable);
}
