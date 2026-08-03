package com.medflow.modules.tenancy.domain;

import com.medflow.modules.tenancy.api.EntitlementStatus;
import com.medflow.shared.persistence.LowerCaseEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EntitlementStatusConverter extends LowerCaseEnumConverter<EntitlementStatus> {

  public EntitlementStatusConverter() {
    super(EntitlementStatus.class);
  }
}
