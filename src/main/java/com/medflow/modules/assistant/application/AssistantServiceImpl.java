package com.medflow.modules.assistant.application;

import com.medflow.modules.assistant.api.AssistantService;
import com.medflow.modules.assistant.api.ChatRole;
import com.medflow.modules.assistant.api.request.SendMessageRequest;
import com.medflow.modules.assistant.api.response.ChatMessageResponse;
import com.medflow.modules.assistant.domain.AssistantResponder;
import com.medflow.modules.assistant.domain.entity.ChatMessage;
import com.medflow.modules.assistant.domain.repository.ChatMessageRepository;
import com.medflow.shared.api.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AssistantServiceImpl implements AssistantService {

  private static final int MAX_PAGE_SIZE = 100;

  private final ChatMessageRepository repository;
  private final AssistantResponder responder;

  AssistantServiceImpl(ChatMessageRepository repository, AssistantResponder responder) {
    this.repository = repository;
    this.responder = responder;
  }

  @Override
  @Transactional
  public ChatMessageResponse send(Long hospitalId, Long userId, SendMessageRequest request) {
    var conversationId = (request.conversationId() == null || request.conversationId().isBlank())
        ? UUID.randomUUID().toString()
        : request.conversationId();
    repository.save(new ChatMessage(hospitalId, userId, conversationId, ChatRole.USER,
        request.content()));
    var reply = repository.save(new ChatMessage(hospitalId, userId, conversationId,
        ChatRole.ASSISTANT, responder.reply(request.content())));
    return toResponse(reply);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<ChatMessageResponse> history(Long userId, String conversationId, int page,
      int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var result = (conversationId == null || conversationId.isBlank())
        ? repository.findByUserId(userId, pageable)
        : repository.findByUserIdAndConversationId(userId, conversationId, pageable);
    return PageResponse.from(result.map(this::toResponse));
  }

  private ChatMessageResponse toResponse(ChatMessage message) {
    return new ChatMessageResponse(message.getId(), message.getConversationId(), message.getRole(),
        message.getContent(), message.getCreatedAt());
  }
}
