package com.medflow.modules.rbac.application;

import com.medflow.modules.rbac.api.AccessControlService;
import com.medflow.modules.rbac.api.RoleSummary;
import com.medflow.modules.rbac.api.UserGroupSummary;
import com.medflow.modules.rbac.api.response.RoleResponse;
import com.medflow.modules.rbac.api.response.UserGroupResponse;
import com.medflow.modules.rbac.domain.entity.Role;
import com.medflow.modules.rbac.domain.entity.UserGroup;
import com.medflow.modules.rbac.domain.entity.UserGroupPortalAccess;
import com.medflow.modules.rbac.domain.repository.RolePermissionRepository;
import com.medflow.modules.rbac.domain.repository.RoleRepository;
import com.medflow.modules.rbac.domain.repository.UserGroupPortalAccessRepository;
import com.medflow.modules.rbac.domain.repository.UserGroupRepository;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AccessControlServiceImpl implements AccessControlService {

  private final RoleRepository roleRepository;
  private final RolePermissionRepository rolePermissionRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserGroupPortalAccessRepository portalAccessRepository;

  AccessControlServiceImpl(RoleRepository roleRepository,
      RolePermissionRepository rolePermissionRepository, UserGroupRepository userGroupRepository,
      UserGroupPortalAccessRepository portalAccessRepository) {
    this.roleRepository = roleRepository;
    this.rolePermissionRepository = rolePermissionRepository;
    this.userGroupRepository = userGroupRepository;
    this.portalAccessRepository = portalAccessRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoleResponse> roles() {
    Map<Integer, List<String>> grants = rolePermissionRepository.findDefaultGrants().stream()
        .collect(Collectors.groupingBy(row -> (Integer) row[0],
            Collectors.mapping(row -> (String) row[1], Collectors.toList())));
    return roleRepository.findAll().stream()
        .map(role -> new RoleResponse(role.getId(), role.getRoleCode(), role.getRoleName(),
            role.getDescription(), role.isSystemRole(),
            grants.getOrDefault(role.getId(), List.of())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserGroupResponse> userGroups() {
    Map<Integer, List<Integer>> modules = portalAccessRepository.findByCanAccessTrue().stream()
        .collect(Collectors.groupingBy(UserGroupPortalAccess::getUserGroupId,
            Collectors.mapping(UserGroupPortalAccess::getModuleId, Collectors.toList())));
    return userGroupRepository.findAll().stream()
        .map(group -> new UserGroupResponse(group.getId(), group.getGroupCode(),
            group.getGroupName(), group.getDescription(), group.isActive(),
            modules.getOrDefault(group.getId(), List.of())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public RoleSummary requireRoleByCode(String roleCode) {
    return roleRepository.findByRoleCode(roleCode).map(this::toSummary)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleCode));
  }

  @Override
  @Transactional(readOnly = true)
  public RoleSummary requireRoleById(Integer roleId) {
    return roleRepository.findById(roleId).map(this::toSummary)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoleSummary> rolesByIds(Collection<Integer> roleIds) {
    return roleRepository.findAllById(roleIds).stream().map(this::toSummary).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public UserGroupSummary requireGroupByCode(String groupCode) {
    return userGroupRepository.findByGroupCode(groupCode).map(this::toSummary)
        .orElseThrow(() -> new ResourceNotFoundException("User group not found: " + groupCode));
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> permissionCodes(Integer roleId, Long hospitalId) {
    return rolePermissionRepository.findPermissionCodes(roleId, hospitalId);
  }

  private RoleSummary toSummary(Role role) {
    return new RoleSummary(role.getId(), role.getRoleCode(), role.getRoleName());
  }

  private UserGroupSummary toSummary(UserGroup group) {
    return new UserGroupSummary(group.getId(), group.getGroupCode(), group.getGroupName());
  }
}
