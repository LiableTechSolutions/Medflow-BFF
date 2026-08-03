package com.medflow.shared.persistence;

import com.medflow.shared.domain.Gender;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter extends LowerCaseEnumConverter<Gender> {

  public GenderConverter() {
    super(Gender.class);
  }
}
