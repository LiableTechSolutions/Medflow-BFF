package com.medflow.config;

import com.medflow.shared.security.PublicApi;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import java.util.*;

@Configuration @EnableMethodSecurity
class SecurityConfiguration {
  @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, PublicApiRequestMatcher publicApi) throws Exception {
    return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.requestMatchers(publicApi).permitAll().requestMatchers("/swagger-ui/**", "/api-docs/**", "/actuator/health/**").permitAll().anyRequest().authenticated()).httpBasic(basic -> {}).build();
  }
  @Bean PublicApiRequestMatcher publicApiRequestMatcher(List<AbstractHandlerMethodMapping<?>> mappings) { return new PublicApiRequestMatcher(mappings); }
  static final class PublicApiRequestMatcher implements RequestMatcher {
    private final List<AbstractHandlerMethodMapping<?>> mappings;
    PublicApiRequestMatcher(List<AbstractHandlerMethodMapping<?>> mappings) { this.mappings = mappings; }
    public boolean matches(jakarta.servlet.http.HttpServletRequest request) {
      return mappings.stream().map(m -> handler(m, request)).filter(Objects::nonNull).filter(HandlerMethod.class::isInstance).map(HandlerMethod.class::cast).anyMatch(h -> h.hasMethodAnnotation(PublicApi.class) || h.getBeanType().isAnnotationPresent(PublicApi.class));
    }
    private Object handler(AbstractHandlerMethodMapping<?> mapping, jakarta.servlet.http.HttpServletRequest request) { try { var chain = mapping.getHandler(request); return chain == null ? null : chain.getHandler(); } catch (Exception ignored) { return null; } }
  }
}
