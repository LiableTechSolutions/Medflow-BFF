package com.medflow.config;

import com.medflow.shared.security.PublicApi;
import com.medflow.shared.security.SecurityProperties;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Stateless JWT security. Endpoints are private by default; opt out explicitly with
 * {@link PublicApi}. Tokens are HS256-signed and carry the user's role, which method
 * security consumes via {@code hasRole(...)}.
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
class SecurityConfiguration {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, PublicApiRequestMatcher publicApi,
      CorsConfigurationSource corsConfigurationSource) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(publicApi).permitAll()
            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**",
                "/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(SecurityProperties properties) {
    var configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(properties.cors().allowedOrigins());
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "X-Trace-Id"));
    configuration.setExposedHeaders(List.of("X-Trace-Id"));
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  JwtEncoder jwtEncoder(SecurityProperties properties) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
  }

  @Bean
  JwtDecoder jwtDecoder(SecurityProperties properties) {
    return NimbusJwtDecoder.withSecretKey(secretKey(properties))
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
  }

  private SecretKeySpec secretKey(SecurityProperties properties) {
    return new SecretKeySpec(properties.jwt().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    var authorities = new JwtGrantedAuthoritiesConverter();
    authorities.setAuthoritiesClaimName("role");
    authorities.setAuthorityPrefix("ROLE_");
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authorities);
    return converter;
  }

  @Bean
  PublicApiRequestMatcher publicApiRequestMatcher(List<AbstractHandlerMethodMapping<?>> mappings) {
    return new PublicApiRequestMatcher(mappings);
  }

  /** Matches any handler method (or controller) annotated with {@link PublicApi}. */
  static final class PublicApiRequestMatcher implements RequestMatcher {
    private final List<AbstractHandlerMethodMapping<?>> mappings;

    PublicApiRequestMatcher(List<AbstractHandlerMethodMapping<?>> mappings) {
      this.mappings = mappings;
    }

    @Override
    public boolean matches(jakarta.servlet.http.HttpServletRequest request) {
      return mappings.stream()
          .map(mapping -> handler(mapping, request))
          .filter(Objects::nonNull)
          .filter(HandlerMethod.class::isInstance)
          .map(HandlerMethod.class::cast)
          .anyMatch(handler -> handler.hasMethodAnnotation(PublicApi.class)
              || handler.getBeanType().isAnnotationPresent(PublicApi.class));
    }

    private Object handler(AbstractHandlerMethodMapping<?> mapping,
        jakarta.servlet.http.HttpServletRequest request) {
      try {
        var chain = mapping.getHandler(request);
        return chain == null ? null : chain.getHandler();
      } catch (Exception ignored) {
        return null;
      }
    }
  }
}
