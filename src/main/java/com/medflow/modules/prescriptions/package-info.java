/**
 * Prescriptions module: digitally signed clinical records. Medication lines are stored as
 * a JSON document on the prescription itself so the record reads back exactly as it was
 * written, independent of the pharmacy catalogue.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Prescriptions")
package com.medflow.modules.prescriptions;
