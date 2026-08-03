/**
 * Audit module: an append-only record of who did what, in which tenant. Other modules
 * write to it through {@code AuditService}; nothing ever updates or deletes a row.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Audit")
package com.medflow.modules.audit;
