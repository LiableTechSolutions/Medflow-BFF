package com.medflow.modules.rbac.domain.repository;

import com.medflow.modules.rbac.domain.entity.UserGroupPortalAccess;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupPortalAccessRepository extends JpaRepository<UserGroupPortalAccess, Long> {

  List<UserGroupPortalAccess> findByCanAccessTrue();
}
