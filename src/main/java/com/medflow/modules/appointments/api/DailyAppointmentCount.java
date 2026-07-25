package com.medflow.modules.appointments.api;

import java.time.LocalDate;

/** One point of the dashboard's weekly activity chart. */
public record DailyAppointmentCount(LocalDate date, long count) {
}
