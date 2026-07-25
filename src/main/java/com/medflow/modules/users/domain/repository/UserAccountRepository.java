package com.medflow.modules.users.domain.repository;

import com.medflow.modules.users.domain.entity.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

  Optional<UserAccount> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  Optional<UserAccount> findByResetToken(String resetToken);

  @Query("""
      SELECT u FROM UserAccount u
      WHERE :query IS NULL
         OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
         OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
      """)
  Page<UserAccount> search(@Param("query") String query, Pageable pageable);
}
