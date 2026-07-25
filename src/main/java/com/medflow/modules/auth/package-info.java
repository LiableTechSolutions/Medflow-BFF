/**
 * Authentication module: credential exchange, JWT issuance and the password reset flow.
 * Stateless by design — account state lives in the {@code users} module, token
 * validation is handled by the resource-server filter chain.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Auth")
package com.medflow.modules.auth;
