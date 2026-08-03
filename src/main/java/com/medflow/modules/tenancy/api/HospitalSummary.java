package com.medflow.modules.tenancy.api;

/** Lightweight projection used by other modules to label a tenant. */
public record HospitalSummary(Long id, String hospitalCode, String name, String timezone) {
}
