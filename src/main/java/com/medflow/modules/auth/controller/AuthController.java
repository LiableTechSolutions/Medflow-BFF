package com.medflow.modules.auth.controller;

import com.medflow.modules.auth.api.AuthService;
import com.medflow.modules.auth.api.request.ForgotPasswordRequest;
import com.medflow.modules.auth.api.request.LoginRequest;
import com.medflow.modules.auth.api.request.RegisterRequest;
import com.medflow.modules.auth.api.request.ResetPasswordRequest;
import com.medflow.modules.auth.api.response.AuthResponse;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.shared.api.ApiResponse;
import com.medflow.shared.security.PublicApi;
import com.medflow.shared.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

  private final AuthService service;
  private final TenantContext tenantContext;

  AuthController(AuthService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  @PostMapping("/register")
  @PublicApi
  @Operation(summary = "Create workspace",
      description = "Creates a hospital plus its first administrator and returns an access token.")
  ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Workspace created successfully", service.register(request)));
  }

  @PostMapping("/login")
  @PublicApi
  @Operation(summary = "Sign in", description = "Exchanges credentials for a bearer token.")
  ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.success("Signed in successfully", service.login(request));
  }

  @PostMapping("/forgot-password")
  @PublicApi
  @Operation(summary = "Request password reset",
      description = "Always responds identically whether or not the email exists.")
  ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    service.requestPasswordReset(request.email());
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(ApiResponse.success("If an account matches that email, a reset link is on its way", null));
  }

  @PostMapping("/reset-password")
  @PublicApi
  @Operation(summary = "Reset password", description = "Consumes a reset token and sets a new password.")
  ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    service.resetPassword(request);
    return ApiResponse.success("Password has been reset successfully", null);
  }

  @GetMapping("/me")
  @Operation(summary = "Current user", description = "Returns the profile of the authenticated user.")
  ApiResponse<UserAccountResponse> me() {
    return ApiResponse.success("Profile retrieved successfully",
        service.currentUser(tenantContext.userId()));
  }
}
