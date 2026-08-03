package com.medflow.modules.assistant.domain.entity;

import com.medflow.modules.assistant.api.ChatRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "conversation_id", nullable = false, length = 36)
  private String conversationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ChatRole role;

  @Column(nullable = false, length = 4000)
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected ChatMessage() {
  }

  public ChatMessage(Long hospitalId, Long userId, String conversationId, ChatRole role,
      String content) {
    this.hospitalId = hospitalId;
    this.userId = userId;
    this.conversationId = conversationId;
    this.role = role;
    this.content = content;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public Long getUserId() { return userId; }
  public String getConversationId() { return conversationId; }
  public ChatRole getRole() { return role; }
  public String getContent() { return content; }
  public Instant getCreatedAt() { return createdAt; }
}
