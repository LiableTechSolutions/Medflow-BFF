/**
 * Prescriptions module: issuing prescriptions with medication line items and tracing a
 * patient's prescribing history. Medication stock lives in the pharmacy module; line
 * items capture the prescribed drug as text so a prescription remains a faithful
 * clinical record even if the catalog changes.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Prescriptions")
package com.medflow.modules.prescriptions;
