package com.medflow.modules.analytics.api.response;

import java.io.Serializable;
import java.time.LocalDate;

public record DailyActivityResponse(LocalDate date, long visits) implements Serializable {
}
