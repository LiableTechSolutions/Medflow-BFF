/**
 * Analytics module: read-only aggregates for the dashboard and reports pages. Owns no
 * tables — it composes numbers exclusively from other modules' public APIs, and caches
 * the dashboard summary briefly in Redis.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Analytics")
package com.medflow.modules.analytics;
