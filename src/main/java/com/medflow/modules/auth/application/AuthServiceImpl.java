package com.medflow.modules.auth.application;

import com.medflow.modules.auth.api.AuthService;
import com.medflow.modules.auth.api.request.LoginRequest;
import com.medflow.modules.auth.api.request.RegisterRequest;
import com.medflow.modules.auth.api.request.ResetPasswordRequest;
import com.medflow.modules.auth.api.response.AuthResponse;
import com.medflow.modules.users.api.UserAccountService;
import com.medflow.modules.users.api.request.CreateUserAccountRequest;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.shared.exception.BusinessRuleViolationException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class AuthServiceImpl implements AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

  private final UserAccountService userAccountService;
  private final TokenService tokenService;

  AuthServiceImpl(UserAccountService userAccountService, TokenService tokenService) {
    this.userAccountService = userAccountService;
    this.tokenService = tokenService;
  }

  @Override
  public AuthResponse register(RegisterRequest request) {
    if (!request.password().equals(request.confirmPassword())) {
      throw new BusinessRuleViolationException("Passwords do not match");
    }
    var user = userAccountService.register(new CreateUserAccountRequest(
        request.fullName(), request.email(), request.password(), null));
    return toAuthResponse(user);
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    var user = userAccountService.verifyCredentials(request.email(), request.password());
    return toAuthResponse(user);
  }

  @Override
  public void requestPasswordReset(String email) {
    userAccountService.initiatePasswordReset(email).ifPresent(token ->
        // Placeholder delivery channel until an email adapter is wired in; the token is
        // never returned over the API to avoid account enumeration.
        log.info("Password reset token issued for {}: {}", email, token));
  }

  @Override
  public void resetPassword(ResetPasswordRequest request) {
    userAccountService.resetPassword(request.token(), request.newPassword());
  }

  @Override
  public UserAccountResponse currentUser(UUID userId) {
    return userAccountService.getById(userId);
  }

  private AuthResponse toAuthResponse(UserAccountResponse user) {
    var token = tokenService.issue(user);
    return AuthResponse.bearer(token.value(), token.expiresAt(), user);
  }
}
