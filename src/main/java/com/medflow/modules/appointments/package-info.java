/**
 * Appointments module: booking, confirmation, rescheduling, completion and cancellation.
 * References patients and doctors only through their public APIs (UUID + summaries) and
 * announces bookings/schedule conflicts as domain events consumed by the notifications
 * module.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Appointments")
package com.medflow.modules.appointments;
