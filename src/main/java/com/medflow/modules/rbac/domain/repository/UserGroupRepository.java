package com.medflow.modules.rbac.domain.repository;

import com.medflow.modules.rbac.domain.entity.UserGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupRepository extends JpaRepository<UserGroup, Integer> {

  Optional<UserGroup> findByGroupCode(String groupCode);
}
