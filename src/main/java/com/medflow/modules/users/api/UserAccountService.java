package com.medflow.modules.users.api;

import com.medflow.modules.users.api.request.CreateUserAccountRequest;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.shared.api.PageResponse;
import java.util.Optional;
import java.util.UUID;

/** Public API of the Users module; the only entry point other modules may use. */
public interface UserAccountService {

  /**
   * Registers an account. When {@code request.role()} is null the very first account
   * becomes {@link UserRole#ADMIN} (workspace creator) and later ones default to
   * {@link UserRole#RECEPTIONIST}.
   */
  UserAccountResponse register(CreateUserAccountRequest request);

  /** Verifies email/password and returns the account, or fails with a 401-mapped exception. */
  UserAccountResponse verifyCredentials(String email, String rawPassword);

  UserAccountResponse getById(UUID userId);

  PageResponse<UserAccountResponse> search(String query, int page, int size);

  UserAccountResponse changeRole(UUID userId, UserRole role);

  UserAccountResponse changeStatus(UUID userId, UserStatus status);

  /**
   * Issues a short-lived password reset token. Empty when the email is unknown so
   * callers can respond identically either way (no account enumeration).
   */
  Optional<String> initiatePasswordReset(String email);

  void resetPassword(String token, String newPassword);
}
