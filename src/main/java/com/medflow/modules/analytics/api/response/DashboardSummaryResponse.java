package com.medflow.modules.analytics.api.response;

import java.io.Serializable;
import java.math.BigDecimal;

/** Serializable because the summary is cached in Redis. */
public record DashboardSummaryResponse(
    BigDecimal revenueMonthToDate,
    long totalPatients,
    long doctorsOnStaff,
    long appointmentsToday,
    long labReportsCompleted,
    long activeCases) implements Serializable {
}
