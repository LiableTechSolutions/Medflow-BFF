/**
 * Appointments module: booking, the front-desk queue and the consultation workflow
 * (booked → confirmed → checked in → in consultation → completed). References patients,
 * doctors and tenancy only through their public APIs, and announces bookings and
 * schedule conflicts as events the notifications module consumes.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Appointments")
package com.medflow.modules.appointments;
