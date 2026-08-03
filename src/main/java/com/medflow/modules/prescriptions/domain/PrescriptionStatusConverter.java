package com.medflow.modules.prescriptions.domain;

import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.shared.persistence.LowerCaseEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PrescriptionStatusConverter extends LowerCaseEnumConverter<PrescriptionStatus> {

  public PrescriptionStatusConverter() {
    super(PrescriptionStatus.class);
  }
}
