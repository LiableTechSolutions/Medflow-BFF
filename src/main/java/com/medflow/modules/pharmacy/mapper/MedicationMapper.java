package com.medflow.modules.pharmacy.mapper;

import com.medflow.modules.pharmacy.api.response.MedicationResponse;
import com.medflow.modules.pharmacy.domain.entity.Medication;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicationMapper {

  MedicationResponse toResponse(Medication medication);
}
