package com.medflow.shared.persistence;

import com.medflow.shared.domain.AccountStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountStatusConverter extends LowerCaseEnumConverter<AccountStatus> {

  public AccountStatusConverter() {
    super(AccountStatus.class);
  }
}
