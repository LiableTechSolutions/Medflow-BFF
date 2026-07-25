package com.medflow.modules.appointments.mapper;

import com.medflow.modules.appointments.api.response.AppointmentResponse;
import com.medflow.modules.appointments.domain.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

  @Mapping(target = "patientName", source = "patientName")
  @Mapping(target = "doctorName", source = "doctorName")
  AppointmentResponse toResponse(Appointment appointment, String patientName, String doctorName);
}
