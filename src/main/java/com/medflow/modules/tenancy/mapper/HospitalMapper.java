package com.medflow.modules.tenancy.mapper;

import com.medflow.modules.tenancy.api.HospitalSummary;
import com.medflow.modules.tenancy.api.response.HospitalResponse;
import com.medflow.modules.tenancy.domain.entity.Hospital;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HospitalMapper {

  HospitalResponse toResponse(Hospital hospital);

  HospitalSummary toSummary(Hospital hospital);
}
