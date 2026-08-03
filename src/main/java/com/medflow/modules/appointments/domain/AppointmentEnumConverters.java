package com.medflow.modules.appointments.domain;

import com.medflow.modules.appointments.api.AppointmentMode;
import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.shared.persistence.LowerCaseEnumConverter;
import jakarta.persistence.Converter;

/** Persists the scheduling enums using the exact values from the database design. */
public final class AppointmentEnumConverters {

  private AppointmentEnumConverters() {
  }

  @Converter(autoApply = true)
  public static class StatusConverter extends LowerCaseEnumConverter<AppointmentStatus> {
    public StatusConverter() {
      super(AppointmentStatus.class);
    }
  }

  @Converter(autoApply = true)
  public static class ModeConverter extends LowerCaseEnumConverter<AppointmentMode> {
    public ModeConverter() {
      super(AppointmentMode.class);
    }
  }
}
