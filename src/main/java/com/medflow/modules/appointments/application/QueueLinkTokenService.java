package com.medflow.modules.appointments.application;

import com.medflow.shared.security.SecurityProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

/**
 * Signs and verifies the opaque {@code token} in a public queue-board link, so the link
 * can carry hospital/doctor/date without exposing guessable ids or requiring a login.
 * Reuses the app's existing HS256 signing key ({@link JwtEncoder}/{@link JwtDecoder} are
 * framework beans, not owned by the auth module) rather than adding a second secret to
 * manage. A token is bound to one doctor's one day and stops verifying once that day has
 * passed - not a general-purpose credential.
 */
@Component
class QueueLinkTokenService {

  private static final String SUBJECT = "public-queue";

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final SecurityProperties properties;
  private final Clock clock;

  QueueLinkTokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, SecurityProperties properties,
      Clock clock) {
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.properties = properties;
    this.clock = clock;
  }

  String issue(String hospitalCode, Long doctorId, LocalDate date) {
    var now = Instant.now(clock);
    // A day of slack past midnight covers timezone/clock skew without the link
    // outliving its purpose by more than a day.
    var expiresAt = date.plusDays(2).atStartOfDay(ZoneOffset.UTC).toInstant();
    var claims = JwtClaimsSet.builder()
        .issuer(properties.jwt().issuer())
        .issuedAt(now)
        .expiresAt(expiresAt)
        .subject(SUBJECT)
        .claim("hospitalCode", hospitalCode)
        .claim("doctorId", doctorId)
        .claim("date", date.toString())
        .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  /** @throws AccessDeniedException if the token is malformed, forged, or expired. */
  DecodedQueueLink verify(String token) {
    try {
      var jwt = jwtDecoder.decode(token);
      if (!SUBJECT.equals(jwt.getSubject())) {
        throw new AccessDeniedException("Invalid queue link");
      }
      var doctorId = jwt.getClaim("doctorId");
      return new DecodedQueueLink(jwt.getClaimAsString("hospitalCode"),
          doctorId == null ? null : ((Number) doctorId).longValue(),
          LocalDate.parse(jwt.getClaimAsString("date")));
    } catch (JwtException e) {
      throw new AccessDeniedException("This queue link is invalid or has expired");
    }
  }

  record DecodedQueueLink(String hospitalCode, Long doctorId, LocalDate date) {
  }
}
