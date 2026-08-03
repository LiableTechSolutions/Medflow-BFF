package com.medflow.modules.rbac.domain.repository;

import com.medflow.modules.rbac.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {
}
