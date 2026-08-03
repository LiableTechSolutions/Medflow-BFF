package com.medflow.modules.rbac.api;

import com.medflow.modules.rbac.api.response.RoleResponse;
import com.medflow.modules.rbac.api.response.UserGroupResponse;
import java.util.Collection;
import java.util.List;

/** Public API of the Access Control module. */
public interface AccessControlService {

  List<RoleResponse> roles();

  List<UserGroupResponse> userGroups();

  /** Resolves a seeded role by its code, e.g. {@link RoleCodes#DOCTOR}. */
  RoleSummary requireRoleByCode(String roleCode);

  RoleSummary requireRoleById(Integer roleId);

  List<RoleSummary> rolesByIds(Collection<Integer> roleIds);

  UserGroupSummary requireGroupByCode(String groupCode);

  /**
   * Permission codes a role carries, including any hospital-specific grants. Baked into
   * the access token so authorization needs no database round-trip per request.
   */
  List<String> permissionCodes(Integer roleId, Long hospitalId);
}
