package com.medflow.modules.users.domain.repository;

import com.medflow.modules.users.domain.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

  Optional<UserAccount> findByEmailIgnoreCaseAndDeletedFalse(String email);

  boolean existsByEmailIgnoreCase(String email);

  Optional<UserAccount> findByIdAndDeletedFalse(Long id);

  Optional<UserAccount> findByIdAndHospitalIdAndDeletedFalse(Long id, Long hospitalId);

  Optional<UserAccount> findByResetToken(String resetToken);

  long countByHospitalIdAndDeletedFalse(Long hospitalId);

  @Query("""
      SELECT u FROM UserAccount u
      WHERE u.hospitalId = :hospitalId
        AND u.deleted = FALSE
        AND (:roleId IS NULL OR u.roleId = :roleId)
        AND (CAST(:query AS string) IS NULL
             OR LOWER(CONCAT(u.firstName, ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
             OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
      """)
  Page<UserAccount> search(@Param("hospitalId") Long hospitalId, @Param("query") String query,
      @Param("roleId") Integer roleId, Pageable pageable);

  @Query("""
      SELECT u.id FROM UserAccount u
      WHERE u.hospitalId = :hospitalId
        AND u.deleted = FALSE
        AND (LOWER(CONCAT(u.firstName, ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
             OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
      """)
  java.util.List<Long> findIdsMatching(@Param("hospitalId") Long hospitalId,
      @Param("query") String query);
}
