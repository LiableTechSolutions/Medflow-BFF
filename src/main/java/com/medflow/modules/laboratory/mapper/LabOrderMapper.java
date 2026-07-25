package com.medflow.modules.laboratory.mapper;

import com.medflow.modules.laboratory.api.response.LabOrderResponse;
import com.medflow.modules.laboratory.domain.entity.LabOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LabOrderMapper {

  @Mapping(target = "patientName", source = "patientName")
  @Mapping(target = "orderedByName", source = "orderedByName")
  LabOrderResponse toResponse(LabOrder labOrder, String patientName, String orderedByName);
}
