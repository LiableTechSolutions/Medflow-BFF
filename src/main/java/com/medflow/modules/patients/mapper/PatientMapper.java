package com.medflow.modules.patients.mapper;
import com.medflow.modules.patients.api.response.PatientResponse; import com.medflow.modules.patients.domain.entity.Patient; import org.mapstruct.Mapper;
@Mapper(componentModel = "spring") public interface PatientMapper { PatientResponse toResponse(Patient patient); }
