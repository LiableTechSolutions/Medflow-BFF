package com.medflow.modules.rbac.api.response;

import java.util.List;

/** A portal and the ids of the product modules it exposes. */
public record UserGroupResponse(
    Integer id,
    String groupCode,
    String groupName,
    String description,
    boolean active,
    List<Integer> accessibleModuleIds) {
}
