package com.medflow.modules.assistant.domain.entity;

import com.medflow.modules.assistant.api.ChatRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages", indexes =
    @Index(name = "idx_chat_messages_thread", columnList = "user_id, conversation_id, created_at"))
public class ChatMessage {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "conversation_id", nullable = false)
  private UUID conversationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ChatRole role;

  @Column(nullable = false, length = 4000)
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected ChatMessage() {
  }

  public ChatMessage(UUID userId, UUID conversationId, ChatRole role, String content) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.conversationId = conversationId;
    this.role = role;
    this.content = content;
    this.createdAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public UUID getConversationId() { return conversationId; }
  public ChatRole getRole() { return role; }
  public String getContent() { return content; }
  public Instant getCreatedAt() { return createdAt; }
}
