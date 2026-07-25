package com.medflow.shared.security;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed access to the {@code medflow.security.*} configuration tree. */
@ConfigurationProperties(prefix = "medflow.security")
public record SecurityProperties(Jwt jwt, Cors cors) {

  public record Jwt(String secret, String issuer, Duration accessTokenTtl) { }

  public record Cors(List<String> allowedOrigins) { }
}
