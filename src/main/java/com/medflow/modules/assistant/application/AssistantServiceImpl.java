package com.medflow.modules.assistant.application;

import com.medflow.modules.assistant.api.AssistantService;
import com.medflow.modules.assistant.api.ChatRole;
import com.medflow.modules.assistant.api.request.SendMessageRequest;
import com.medflow.modules.assistant.api.response.ChatMessageResponse;
import com.medflow.modules.assistant.domain.AssistantResponder;
import com.medflow.modules.assistant.domain.entity.ChatMessage;
import com.medflow.modules.assistant.domain.repository.ChatMessageRepository;
import com.medflow.modules.assistant.mapper.ChatMessageMapper;
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
  private final ChatMessageMapper mapper;
  private final AssistantResponder responder;

  AssistantServiceImpl(ChatMessageRepository repository, ChatMessageMapper mapper,
      AssistantResponder responder) {
    this.repository = repository;
    this.mapper = mapper;
    this.responder = responder;
  }

  @Override
  @Transactional
  public ChatMessageResponse send(UUID userId, SendMessageRequest request) {
    var conversationId = request.conversationId() != null
        ? request.conversationId()
        : UUID.randomUUID();
    repository.save(new ChatMessage(userId, conversationId, ChatRole.USER, request.content()));
    var reply = repository.save(new ChatMessage(userId, conversationId, ChatRole.ASSISTANT,
        responder.reply(request.content())));
    return mapper.toResponse(reply);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<ChatMessageResponse> history(UUID userId, UUID conversationId, int page,
      int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var result = conversationId != null
        ? repository.findByUserIdAndConversationId(userId, conversationId, pageable)
        : repository.findByUserId(userId, pageable);
    return PageResponse.from(result.map(mapper::toResponse));
  }
}
