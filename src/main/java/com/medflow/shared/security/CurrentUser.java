package com.medflow.shared.security;

/**
 * The authenticated principal, projected from the JWT. {@code hospitalId} is the tenant
 * every query in the request is scoped to.
 */
public record CurrentUser(
    Long userId,
    Long hospitalId,
    String email,
    String fullName,
    String roleCode) {
}
