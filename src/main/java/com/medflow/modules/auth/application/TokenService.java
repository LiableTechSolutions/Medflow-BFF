package com.medflow.modules.auth.application;

import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.shared.security.SecurityProperties;
import com.medflow.shared.security.TenantContext;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Issues HS256 access tokens. The subject is the user id; the token also carries the
 * tenant ({@code hospitalId}), the role that drives {@code hasRole(...)} checks and the
 * permission codes the UI uses to decide which actions to render.
 */
@Component
class TokenService {

  private final JwtEncoder jwtEncoder;
  private final SecurityProperties properties;
  private final Clock clock;

  TokenService(JwtEncoder jwtEncoder, SecurityProperties properties, Clock clock) {
    this.jwtEncoder = jwtEncoder;
    this.properties = properties;
    this.clock = clock;
  }

  IssuedToken issue(UserAccountResponse user, List<String> permissions) {
    var now = Instant.now(clock);
    var expiresAt = now.plus(properties.jwt().accessTokenTtl());
    var claims = JwtClaimsSet.builder()
        .issuer(properties.jwt().issuer())
        .issuedAt(now)
        .expiresAt(expiresAt)
        .subject(String.valueOf(user.id()))
        .claim(TenantContext.HOSPITAL_CLAIM, user.hospitalId())
        .claim(TenantContext.EMAIL_CLAIM, user.email())
        .claim(TenantContext.NAME_CLAIM, user.fullName())
        .claim(TenantContext.ROLE_CLAIM, user.roleCode())
        .claim("permissions", permissions)
        .claim("userUid", user.userUid())
        .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new IssuedToken(token, expiresAt);
  }

  record IssuedToken(String value, Instant expiresAt) { }
}
