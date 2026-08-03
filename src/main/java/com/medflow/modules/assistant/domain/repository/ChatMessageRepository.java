package com.medflow.modules.assistant.domain.repository;

import com.medflow.modules.assistant.domain.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  Page<ChatMessage> findByUserId(Long userId, Pageable pageable);

  Page<ChatMessage> findByUserIdAndConversationId(Long userId, String conversationId,
      Pageable pageable);
}
