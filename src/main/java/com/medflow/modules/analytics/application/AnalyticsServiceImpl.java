package com.medflow.modules.analytics.application;

import com.medflow.modules.analytics.api.AnalyticsService;
import com.medflow.modules.analytics.api.response.DailyActivityResponse;
import com.medflow.modules.analytics.api.response.DashboardSummaryResponse;
import com.medflow.modules.appointments.api.AppointmentService;
import com.medflow.modules.doctors.api.DoctorService;
import com.medflow.modules.laboratory.api.LabOrderService;
import com.medflow.modules.patients.api.PatientService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AnalyticsServiceImpl implements AnalyticsService {

  private final PatientService patientService;
  private final DoctorService doctorService;
  private final AppointmentService appointmentService;
  private final LabOrderService labOrderService;
  private final Clock clock;

  AnalyticsServiceImpl(PatientService patientService, DoctorService doctorService,
      AppointmentService appointmentService, LabOrderService labOrderService, Clock clock) {
    this.patientService = patientService;
    this.doctorService = doctorService;
    this.appointmentService = appointmentService;
    this.labOrderService = labOrderService;
    this.clock = clock;
  }

  @Override
  @Cacheable(cacheNames = "dashboard", key = "'summary'")
  @Transactional(readOnly = true)
  public DashboardSummaryResponse getDashboardSummary() {
    var today = LocalDate.now(clock);
    var monthStart = YearMonth.from(today).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    var tomorrow = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    return new DashboardSummaryResponse(
        appointmentService.completedRevenueBetween(monthStart, tomorrow),
        patientService.count(),
        doctorService.count(),
        appointmentService.countOnDate(today),
        labOrderService.countCompleted(),
        appointmentService.countActive());
  }

  @Override
  @Transactional(readOnly = true)
  public List<DailyActivityResponse> getActivity(int days) {
    var today = LocalDate.now(clock);
    var from = today.minusDays(days - 1L);
    return appointmentService.dailyCounts(from, today).stream()
        .map(count -> new DailyActivityResponse(count.date(), count.count()))
        .toList();
  }
}
