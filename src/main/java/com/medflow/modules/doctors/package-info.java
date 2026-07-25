/**
 * Doctors module: staff roster, specialties, departments and availability. Exposes
 * batch summaries so scheduling modules can render doctor names without coupling to
 * this module's persistence model.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Doctors")
package com.medflow.modules.doctors;
