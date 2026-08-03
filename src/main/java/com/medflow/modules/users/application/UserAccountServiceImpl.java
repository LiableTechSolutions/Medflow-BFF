package com.medflow.modules.users.application;

import com.medflow.modules.rbac.api.AccessControlService;
import com.medflow.modules.rbac.api.GroupCodes;
import com.medflow.modules.rbac.api.RoleCodes;
import com.medflow.modules.rbac.api.RoleSummary;
import com.medflow.modules.users.api.UserAccountService;
import com.medflow.modules.users.api.UserSummary;
import com.medflow.modules.users.api.request.CreateUserAccountRequest;
import com.medflow.modules.users.api.request.UpdateUserProfileRequest;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.modules.users.domain.entity.UserAccount;
import com.medflow.modules.users.domain.repository.UserAccountRepository;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.domain.AccountStatus;
import com.medflow.shared.exception.BusinessRuleViolationException;
import com.medflow.shared.exception.DuplicateResourceException;
import com.medflow.shared.exception.InvalidCredentialsException;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserAccountServiceImpl implements UserAccountService {

  private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);
  private static final int MAX_PAGE_SIZE = 100;

  private final UserAccountRepository repository;
  private final AccessControlService accessControl;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final SecureRandom secureRandom = new SecureRandom();

  UserAccountServiceImpl(UserAccountRepository repository, AccessControlService accessControl,
      PasswordEncoder passwordEncoder, Clock clock) {
    this.repository = repository;
    this.accessControl = accessControl;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
  }

  @Override
  @Transactional
  public UserAccountResponse create(Long hospitalId, CreateUserAccountRequest request) {
    if (repository.existsByEmailIgnoreCase(request.email())) {
      throw new DuplicateResourceException("A user with this email already exists");
    }
    var role = accessControl.requireRoleByCode(
        request.roleCode() == null || request.roleCode().isBlank()
            ? RoleCodes.RECEPTIONIST
            : request.roleCode());
    var group = accessControl.requireGroupByCode(defaultGroupCode(request, role));

    var account = repository.save(new UserAccount(hospitalId, role.id(), group.id(),
        request.firstName(), request.lastName(), request.email(), request.phone(),
        passwordEncoder.encode(request.rawPassword()), request.gender(), request.dateOfBirth()));
    return toResponse(account, role);
  }

  @Override
  @Transactional(readOnly = true)
  public UserAccountResponse verifyCredentials(String email, String rawPassword) {
    var account = repository.findByEmailIgnoreCaseAndDeletedFalse(email)
        .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
    if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
      throw new InvalidCredentialsException("Invalid email or password");
    }
    if (!account.getStatus().canSignIn()) {
      throw new InvalidCredentialsException("This account is not active");
    }
    return toResponse(account);
  }

  @Override
  @Transactional
  public void recordLogin(Long userId) {
    repository.findByIdAndDeletedFalse(userId)
        .ifPresent(account -> account.recordLogin(Instant.now(clock)));
  }

  @Override
  @Transactional(readOnly = true)
  public UserAccountResponse getById(Long userId) {
    return toResponse(repository.findByIdAndDeletedFalse(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)));
  }

  @Override
  @Transactional(readOnly = true)
  public UserAccountResponse getById(Long hospitalId, Long userId) {
    return toResponse(load(hospitalId, userId));
  }

  @Override
  @Transactional
  public UserAccountResponse updateProfile(Long hospitalId, Long userId,
      UpdateUserProfileRequest request) {
    var account = load(hospitalId, userId);
    account.updateProfile(request.firstName(), request.lastName(), request.phone(),
        request.gender(), request.dateOfBirth(), request.profilePhotoUrl());
    return toResponse(account);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<UserAccountResponse> search(Long hospitalId, String query, String roleCode,
      int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var normalizedQuery = (query == null || query.isBlank()) ? null : query.trim();
    Integer roleId = (roleCode == null || roleCode.isBlank())
        ? null
        : accessControl.requireRoleByCode(roleCode).id();

    var result = repository.search(hospitalId, normalizedQuery, roleId, pageable);
    var roles = rolesById(result.getContent());
    return new PageResponse<>(
        result.getContent().stream()
            .map(account -> toResponse(account, roles.get(account.getRoleId())))
            .toList(),
        result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
  }

  @Override
  @Transactional
  public UserAccountResponse changeRole(Long hospitalId, Long userId, String roleCode) {
    var account = load(hospitalId, userId);
    var role = accessControl.requireRoleByCode(roleCode);
    var group = accessControl.requireGroupByCode(groupCodeFor(role.roleCode()));
    account.changeRole(role.id(), group.id());
    return toResponse(account, role);
  }

  @Override
  @Transactional
  public UserAccountResponse changeStatus(Long hospitalId, Long userId, AccountStatus status) {
    var account = load(hospitalId, userId);
    account.changeStatus(status);
    return toResponse(account);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserSummary> summariesByIds(Collection<Long> userIds) {
    if (userIds.isEmpty()) {
      return List.of();
    }
    var accounts = repository.findAllById(userIds);
    var roles = rolesById(accounts);
    return accounts.stream()
        .map(account -> new UserSummary(account.getId(), account.getHospitalId(),
            account.getFullName(), account.getEmail(), account.getPhone(),
            roleCodeOf(roles.get(account.getRoleId()))))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> findIdsMatching(Long hospitalId, String query) {
    if (query == null || query.isBlank()) {
      return List.of();
    }
    return repository.findIdsMatching(hospitalId, query.trim());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean emailExists(String email) {
    return repository.existsByEmailIgnoreCase(email);
  }

  @Override
  @Transactional(readOnly = true)
  public long countByHospital(Long hospitalId) {
    return repository.countByHospitalIdAndDeletedFalse(hospitalId);
  }

  @Override
  @Transactional
  public Optional<String> initiatePasswordReset(String email) {
    return repository.findByEmailIgnoreCaseAndDeletedFalse(email).map(account -> {
      var bytes = new byte[32];
      secureRandom.nextBytes(bytes);
      var token = HexFormat.of().formatHex(bytes);
      account.issueResetToken(token, Instant.now(clock).plus(RESET_TOKEN_TTL));
      return token;
    });
  }

  @Override
  @Transactional
  public void resetPassword(String token, String newPassword) {
    var account = repository.findByResetToken(token)
        .orElseThrow(() -> new BusinessRuleViolationException("Reset token is invalid or has expired"));
    if (account.isResetTokenExpired(Instant.now(clock))) {
      throw new BusinessRuleViolationException("Reset token is invalid or has expired");
    }
    account.resetPassword(passwordEncoder.encode(newPassword));
  }

  /** Doctors land in the doctor portal, patients in theirs, everyone else with staff. */
  private String defaultGroupCode(CreateUserAccountRequest request, RoleSummary role) {
    if (request.userGroupCode() != null && !request.userGroupCode().isBlank()) {
      return request.userGroupCode();
    }
    return groupCodeFor(role.roleCode());
  }

  private String groupCodeFor(String roleCode) {
    return switch (roleCode) {
      case RoleCodes.DOCTOR -> GroupCodes.DOCTOR_PORTAL;
      case RoleCodes.PATIENT -> GroupCodes.PATIENT_PORTAL;
      default -> GroupCodes.STAFF_PORTAL;
    };
  }

  private Map<Integer, RoleSummary> rolesById(Collection<UserAccount> accounts) {
    var roleIds = accounts.stream().map(UserAccount::getRoleId).collect(Collectors.toSet());
    return accessControl.rolesByIds(roleIds).stream()
        .collect(Collectors.toMap(RoleSummary::id, Function.identity()));
  }

  private String roleCodeOf(RoleSummary role) {
    return role == null ? null : role.roleCode();
  }

  private UserAccount load(Long hospitalId, Long userId) {
    return repository.findByIdAndHospitalIdAndDeletedFalse(userId, hospitalId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
  }

  private UserAccountResponse toResponse(UserAccount account) {
    return toResponse(account, accessControl.requireRoleById(account.getRoleId()));
  }

  private UserAccountResponse toResponse(UserAccount account, RoleSummary role) {
    return new UserAccountResponse(account.getId(), account.getHospitalId(), account.getUserUid(),
        account.getFirstName(), account.getLastName(), account.getFullName(), account.getEmail(),
        account.getPhone(), account.getRoleId(), role == null ? null : role.roleCode(),
        role == null ? null : role.roleName(), account.getUserGroupId(), account.getGender(),
        account.getDateOfBirth(), account.getProfilePhotoUrl(), account.getStatus(),
        account.getLastLoginAt(), account.getCreatedAt(), account.getUpdatedAt());
  }
}
