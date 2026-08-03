package com.medflow.shared.security;

import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Resolves the calling user and their hospital from the request's JWT.
 *
 * <p>MedFlow is multi-tenant: every table that holds clinical or operational data carries
 * a {@code hospital_id}, and services filter by {@link #hospitalId()} rather than trusting
 * an identifier supplied by the client. Tokens are issued with the tenant baked in, so a
 * caller cannot reach another hospital's rows by guessing ids.
 */
@Component
public class TenantContext {

  public static final String HOSPITAL_CLAIM = "hospitalId";
  public static final String ROLE_CLAIM = "role";
  public static final String NAME_CLAIM = "name";
  public static final String EMAIL_CLAIM = "email";

  /** The caller, or empty for anonymous requests (registration, health checks). */
  public Optional<CurrentUser> current() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken token)) {
      return Optional.empty();
    }
    return Optional.of(fromJwt(token.getToken()));
  }

  /** The caller; fails with 403 when the request is anonymous. */
  public CurrentUser require() {
    return current().orElseThrow(
        () -> new AccessDeniedException("This operation requires an authenticated user"));
  }

  /** Tenant of the current request. */
  public Long hospitalId() {
    return require().hospitalId();
  }

  /** Identifier of the acting user, e.g. for {@code booked_by_user_id} and audit rows. */
  public Long userId() {
    return require().userId();
  }

  private CurrentUser fromJwt(Jwt jwt) {
    var hospitalId = jwt.getClaim(HOSPITAL_CLAIM) == null
        ? null
        : ((Number) jwt.getClaim(HOSPITAL_CLAIM)).longValue();
    return new CurrentUser(
        Long.valueOf(jwt.getSubject()),
        hospitalId,
        jwt.getClaimAsString(EMAIL_CLAIM),
        jwt.getClaimAsString(NAME_CLAIM),
        jwt.getClaimAsString(ROLE_CLAIM));
  }
}
