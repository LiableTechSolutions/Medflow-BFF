package com.medflow.modules.rbac.api;

/** Projection other modules use to resolve a role without touching RBAC tables. */
public record RoleSummary(Integer id, String roleCode, String roleName) {
}
