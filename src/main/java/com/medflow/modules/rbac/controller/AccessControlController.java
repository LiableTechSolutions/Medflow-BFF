package com.medflow.modules.rbac.controller;

import com.medflow.modules.rbac.api.AccessControlService;
import com.medflow.modules.rbac.api.response.RoleResponse;
import com.medflow.modules.rbac.api.response.UserGroupResponse;
import com.medflow.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reference data the User Management screen needs to render its role and portal pickers. */
@RestController
@RequestMapping("/api/v1/access")
class AccessControlController {

  private final AccessControlService service;

  AccessControlController(AccessControlService service) {
    this.service = service;
  }

  @GetMapping("/roles")
  @Operation(summary = "List roles", description = "System roles with the permissions each one grants.")
  ApiResponse<List<RoleResponse>> roles() {
    return ApiResponse.success("Roles retrieved successfully", service.roles());
  }

  @GetMapping("/user-groups")
  @Operation(summary = "List portal groups",
      description = "Portals (staff, doctor, patient) and the modules each one exposes.")
  ApiResponse<List<UserGroupResponse>> userGroups() {
    return ApiResponse.success("User groups retrieved successfully", service.userGroups());
  }
}
