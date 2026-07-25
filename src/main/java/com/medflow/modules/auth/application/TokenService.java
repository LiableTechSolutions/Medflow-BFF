package com.medflow.modules.auth.application;

import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.shared.security.SecurityProperties;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** Issues HS256 access tokens; the subject is the user id, the role claim drives authorization. */
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

  IssuedToken issue(UserAccountResponse user) {
    var now = Instant.now(clock);
    var expiresAt = now.plus(properties.jwt().accessTokenTtl());
    var claims = JwtClaimsSet.builder()
        .issuer(properties.jwt().issuer())
        .issuedAt(now)
        .expiresAt(expiresAt)
        .subject(user.id().toString())
        .claim("email", user.email())
        .claim("name", user.fullName())
        .claim("role", user.role().name())
        .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new IssuedToken(token, expiresAt);
  }

  record IssuedToken(String value, Instant expiresAt) { }
}
