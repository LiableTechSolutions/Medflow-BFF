package com.medflow.modules.rbac.api.response;

import java.util.List;

public record RoleResponse(
    Integer id,
    String roleCode,
    String roleName,
    String description,
    boolean systemRole,
    List<String> permissions) {
}
