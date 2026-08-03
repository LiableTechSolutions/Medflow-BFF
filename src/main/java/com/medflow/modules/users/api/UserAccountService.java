package com.medflow.modules.users.api;

import com.medflow.modules.users.api.request.CreateUserAccountRequest;
import com.medflow.modules.users.api.request.UpdateUserProfileRequest;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.domain.AccountStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Public API of the Users module; the only entry point other modules may use. */
public interface UserAccountService {

  /** Creates an account inside the given hospital. */
  UserAccountResponse create(Long hospitalId, CreateUserAccountRequest request);

  /**
   * Verifies email and password. Sign-in is tenant-agnostic — the account itself carries
   * the hospital, which then scopes every subsequent request.
   */
  UserAccountResponse verifyCredentials(String email, String rawPassword);

  /** Stamps {@code last_login_at} after a successful sign-in. */
  void recordLogin(Long userId);

  UserAccountResponse getById(Long userId);

  UserAccountResponse getById(Long hospitalId, Long userId);

  UserAccountResponse updateProfile(Long hospitalId, Long userId, UpdateUserProfileRequest request);

  PageResponse<UserAccountResponse> search(Long hospitalId, String query, String roleCode,
      int page, int size);

  UserAccountResponse changeRole(Long hospitalId, Long userId, String roleCode);

  UserAccountResponse changeStatus(Long hospitalId, Long userId, AccountStatus status);

  /** Batch name lookup for cross-module display (appointments, doctors, audit). */
  List<UserSummary> summariesByIds(Collection<Long> userIds);

  /**
   * Ids of accounts whose name or email matches, so modules that store only a user id
   * (doctors, patients) can offer name search without reading the users table.
   */
  List<Long> findIdsMatching(Long hospitalId, String query);

  boolean emailExists(String email);

  long countByHospital(Long hospitalId);

  /**
   * Issues a short-lived password reset token. Empty when the email is unknown so callers
   * can respond identically either way (no account enumeration).
   */
  Optional<String> initiatePasswordReset(String email);

  void resetPassword(String token, String newPassword);
}
