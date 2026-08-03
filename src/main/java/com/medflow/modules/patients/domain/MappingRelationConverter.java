package com.medflow.modules.patients.domain;

import com.medflow.modules.patients.api.MappingRelation;
import com.medflow.shared.persistence.LowerCaseEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MappingRelationConverter extends LowerCaseEnumConverter<MappingRelation> {

  public MappingRelationConverter() {
    super(MappingRelation.class);
  }
}
