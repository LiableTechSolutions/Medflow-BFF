package com.medflow.modules.users.controller;

import com.medflow.modules.users.api.UserAccountService;
import com.medflow.modules.users.api.request.ChangeRoleRequest;
import com.medflow.modules.users.api.request.ChangeStatusRequest;
import com.medflow.modules.users.api.request.CreateUserAccountRequest;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
class UserController {

  private final UserAccountService service;

  UserController(UserAccountService service) {
    this.service = service;
  }

  @GetMapping
  @Operation(summary = "List users", description = "Searches the workspace user directory.")
  ApiResponse<PageResponse<UserAccountResponse>> search(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success("Users retrieved successfully", service.search(query, page, size));
  }

  @GetMapping("/{userId}")
  @Operation(summary = "Get user", description = "Returns a user account by identifier.")
  ApiResponse<UserAccountResponse> find(@PathVariable UUID userId) {
    return ApiResponse.success("User retrieved successfully", service.getById(userId));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create user", description = "Admin-only: invites a staff account with an explicit role.")
  ResponseEntity<ApiResponse<UserAccountResponse>> create(
      @Valid @RequestBody CreateUserAccountRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("User created successfully", service.register(request)));
  }

  @PatchMapping("/{userId}/role")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Change role", description = "Admin-only: reassigns the user's workspace role.")
  ApiResponse<UserAccountResponse> changeRole(@PathVariable UUID userId,
      @Valid @RequestBody ChangeRoleRequest request) {
    return ApiResponse.success("User role updated successfully",
        service.changeRole(userId, request.role()));
  }

  @PatchMapping("/{userId}/status")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Change status", description = "Admin-only: activates or disables an account.")
  ApiResponse<UserAccountResponse> changeStatus(@PathVariable UUID userId,
      @Valid @RequestBody ChangeStatusRequest request) {
    return ApiResponse.success("User status updated successfully",
        service.changeStatus(userId, request.status()));
  }
}
