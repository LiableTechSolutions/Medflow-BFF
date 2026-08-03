package com.medflow.modules.rbac.domain.repository;

import com.medflow.modules.rbac.domain.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {

  Optional<Role> findByRoleCode(String roleCode);
}
