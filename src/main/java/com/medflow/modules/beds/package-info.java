/**
 * Beds module (Inpatient and wards): wards with a managed bed count that can be grown
 * or shrunk, and per-bed occupancy. Shrinking only ever removes beds that are free, so
 * a ward can never lose a bed a patient is lying in.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Beds")
package com.medflow.modules.beds;
