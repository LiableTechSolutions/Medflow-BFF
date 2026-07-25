package com.medflow.modules.users.mapper;

import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.modules.users.domain.entity.UserAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAccountMapper {

  UserAccountResponse toResponse(UserAccount account);
}
