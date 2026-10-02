package com.medflow.modules.patients.domain;

import com.medflow.modules.patients.api.HospitalisationStatus;
import com.medflow.shared.persistence.LowerCaseEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class HospitalisationStatusConverter extends LowerCaseEnumConverter<HospitalisationStatus> {

  public HospitalisationStatusConverter() {
    super(HospitalisationStatus.class);
  }
}
