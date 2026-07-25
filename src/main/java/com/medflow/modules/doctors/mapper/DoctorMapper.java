package com.medflow.modules.doctors.mapper;

import com.medflow.modules.doctors.api.DoctorSummary;
import com.medflow.modules.doctors.api.response.DoctorResponse;
import com.medflow.modules.doctors.domain.entity.Doctor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

  DoctorResponse toResponse(Doctor doctor);

  DoctorSummary toSummary(Doctor doctor);
}
