package com.medflow.modules.analytics.api;

import com.medflow.modules.analytics.api.response.DailyActivityResponse;
import com.medflow.modules.analytics.api.response.DashboardSummaryResponse;
import java.util.List;

/** Public API of the Analytics module. */
public interface AnalyticsService {

  /** The KPI cards on the dashboard, computed for "today" in UTC. */
  DashboardSummaryResponse getDashboardSummary();

  /** Daily visit counts for the activity chart; {@code days} ending today, inclusive. */
  List<DailyActivityResponse> getActivity(int days);
}
