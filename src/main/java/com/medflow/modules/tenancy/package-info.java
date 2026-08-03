/**
 * Tenancy module: hospitals (the tenants), the catalogue of product modules and the
 * entitlements that decide which modules a hospital may use. Every other module scopes
 * its data by the hospital id this module owns.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Tenancy")
package com.medflow.modules.tenancy;
