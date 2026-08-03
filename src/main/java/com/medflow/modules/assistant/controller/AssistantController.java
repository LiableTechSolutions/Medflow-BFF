package com.medflow.modules.assistant.controller;

import com.medflow.modules.assistant.api.AssistantService;
import com.medflow.modules.assistant.api.request.SendMessageRequest;
import com.medflow.modules.assistant.api.response.ChatMessageResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant/messages")
class AssistantController {

  private final AssistantService service;
  private final TenantContext tenantContext;

  AssistantController(AssistantService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @PostMapping
  @Operation(summary = "Send message",
      description = "Sends a message to the assistant and returns its reply.")
  ResponseEntity<ApiResponse<ChatMessageResponse>> send(
      @Valid @RequestBody SendMessageRequest request) {
    var user = tenantContext.require();
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
        "Assistant replied successfully",
        service.send(user.hospitalId(), user.userId(), request)));
  }

  @GetMapping
  @Operation(summary = "Chat history",
      description = "The caller's messages, optionally scoped to one conversation.")
  ApiResponse<PageResponse<ChatMessageResponse>> history(
      @RequestParam(required = false) String conversationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Chat history retrieved successfully",
        service.history(tenantContext.userId(), conversationId, page, size));
  }
}
