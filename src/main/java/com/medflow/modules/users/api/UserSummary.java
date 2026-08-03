package com.medflow.modules.users.api;

/** Projection other modules use to render a person's name without joining the users table. */
public record UserSummary(
    Long id,
    Long hospitalId,
    String fullName,
    String email,
    String phone,
    String roleCode) {
}
