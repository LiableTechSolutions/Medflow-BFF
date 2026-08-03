package com.medflow.modules.rbac.domain.repository;

import com.medflow.modules.rbac.domain.entity.RolePermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

  /** Platform-wide grants for the role plus any grant added for this hospital. */
  @Query("""
      SELECT p.permissionCode
      FROM RolePermission rp
      JOIN Permission p ON p.id = rp.permissionId
      WHERE rp.roleId = :roleId
        AND (rp.hospitalId IS NULL OR rp.hospitalId = :hospitalId)
      ORDER BY p.permissionCode
      """)
  List<String> findPermissionCodes(@Param("roleId") Integer roleId,
      @Param("hospitalId") Long hospitalId);

  @Query("""
      SELECT rp.roleId, p.permissionCode
      FROM RolePermission rp
      JOIN Permission p ON p.id = rp.permissionId
      WHERE rp.hospitalId IS NULL
      ORDER BY rp.roleId, p.permissionCode
      """)
  List<Object[]> findDefaultGrants();
}
