package com.medflow.modules.prescriptions.mapper;

import com.medflow.modules.prescriptions.api.response.PrescriptionItemResponse;
import com.medflow.modules.prescriptions.api.response.PrescriptionResponse;
import com.medflow.modules.prescriptions.domain.entity.Prescription;
import com.medflow.modules.prescriptions.domain.entity.PrescriptionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {

  @Mapping(target = "patientName", source = "patientName")
  @Mapping(target = "doctorName", source = "doctorName")
  PrescriptionResponse toResponse(Prescription prescription, String patientName, String doctorName);

  PrescriptionItemResponse toItemResponse(PrescriptionItem item);
}
