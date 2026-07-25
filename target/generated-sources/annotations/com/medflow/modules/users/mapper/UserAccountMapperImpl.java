package com.medflow.modules.users.mapper;

import com.medflow.modules.users.api.UserRole;
import com.medflow.modules.users.api.UserStatus;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.modules.users.domain.entity.UserAccount;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T19:48:22+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserAccountMapperImpl implements UserAccountMapper {

    @Override
    public UserAccountResponse toResponse(UserAccount account) {
        if ( account == null ) {
            return null;
        }

        UUID id = null;
        String fullName = null;
        String email = null;
        UserRole role = null;
        UserStatus status = null;
        Instant createdAt = null;

        id = account.getId();
        fullName = account.getFullName();
        email = account.getEmail();
        role = account.getRole();
        status = account.getStatus();
        createdAt = account.getCreatedAt();

        UserAccountResponse userAccountResponse = new UserAccountResponse( id, fullName, email, role, status, createdAt );

        return userAccountResponse;
    }
}
