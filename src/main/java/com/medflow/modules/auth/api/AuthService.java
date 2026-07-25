package com.medflow.modules.auth.api;

import com.medflow.modules.auth.api.request.LoginRequest;
import com.medflow.modules.auth.api.request.RegisterRequest;
import com.medflow.modules.auth.api.request.ResetPasswordRequest;
import com.medflow.modules.auth.api.response.AuthResponse;
import com.medflow.modules.users.api.response.UserAccountResponse;
import java.util.UUID;

public interface AuthService {

  /** Creates the account and signs the user in immediately (UI navigates straight to the dashboard). */
  AuthResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);

  /** Fire-and-forget: never reveals whether the email exists. */
  void requestPasswordReset(String email);

  void resetPassword(ResetPasswordRequest request);

  UserAccountResponse currentUser(UUID userId);
}
