/**
 * Patients module: the patient master record plus the clinical history and uploaded
 * reports that hang off it, and the mapping that lets a portal account act for a patient
 * (self, parent, guardian, …). Records are archived, never deleted.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Patients")
package com.medflow.modules.patients;
