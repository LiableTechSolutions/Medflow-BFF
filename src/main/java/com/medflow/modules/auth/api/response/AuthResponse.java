package com.medflow.modules.auth.api.response;

import com.medflow.modules.users.api.response.UserAccountResponse;
import java.time.Instant;
import java.util.List;

public record AuthResponse(
    String accessToken,
    String tokenType,
    Instant expiresAt,
    UserAccountResponse user,
    List<String> permissions) {

  public static AuthResponse bearer(String accessToken, Instant expiresAt,
      UserAccountResponse user, List<String> permissions) {
    return new AuthResponse(accessToken, "Bearer", expiresAt, user, permissions);
  }
}
