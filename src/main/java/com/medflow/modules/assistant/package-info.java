/**
 * Assistant module: persisted clinical chat threads per user. Reply generation sits
 * behind the {@code AssistantResponder} port; the default adapter is deterministic and
 * rule-based so the platform runs with zero external dependencies. Swapping in an LLM
 * provider (e.g. the Claude API) means implementing that one interface.
 */
@org.springframework.modulith.ApplicationModule(displayName = "AI Assistant")
package com.medflow.modules.assistant;
