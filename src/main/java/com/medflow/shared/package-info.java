/**
 * Shared kernel: cross-cutting API envelope, exceptions, security markers and web
 * infrastructure. Declared OPEN so every application module may depend on it without
 * per-package named interfaces. Keep this module free of business logic.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Shared Kernel",
    type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.medflow.shared;
