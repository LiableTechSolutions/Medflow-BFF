package com.medflow.modules.users.application;

import com.medflow.modules.users.api.UserAccountService;
import com.medflow.modules.users.api.UserRole;
import com.medflow.modules.users.api.UserStatus;
import com.medflow.modules.users.api.request.CreateUserAccountRequest;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.modules.users.domain.entity.UserAccount;
import com.medflow.modules.users.domain.repository.UserAccountRepository;
import com.medflow.modules.users.mapper.UserAccountMapper;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.exception.BusinessRuleViolationException;
import com.medflow.shared.exception.DuplicateResourceException;
import com.medflow.shared.exception.InvalidCredentialsException;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
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
  private final UserAccountMapper mapper;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final SecureRandom secureRandom = new SecureRandom();

  UserAccountServiceImpl(UserAccountRepository repository, UserAccountMapper mapper,
      PasswordEncoder passwordEncoder, Clock clock) {
    this.repository = repository;
    this.mapper = mapper;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
  }

  @Override
  @Transactional
  public UserAccountResponse register(CreateUserAccountRequest request) {
    if (repository.existsByEmailIgnoreCase(request.email())) {
      throw new DuplicateResourceException("A user with this email already exists");
    }
    var role = request.role() != null ? request.role() : defaultRole();
    var account = new UserAccount(request.fullName(), request.email(),
        passwordEncoder.encode(request.rawPassword()), role);
    return mapper.toResponse(repository.save(account));
  }

  @Override
  @Transactional(readOnly = true)
  public UserAccountResponse verifyCredentials(String email, String rawPassword) {
    var account = repository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
    if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
      throw new InvalidCredentialsException("Invalid email or password");
    }
    if (account.getStatus() == UserStatus.DISABLED) {
      throw new InvalidCredentialsException("This account has been disabled");
    }
    return mapper.toResponse(account);
  }

  @Override
  @Transactional(readOnly = true)
  public UserAccountResponse getById(UUID userId) {
    return mapper.toResponse(load(userId));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<UserAccountResponse> search(String query, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var normalized = (query == null || query.isBlank()) ? null : query.trim();
    return PageResponse.from(repository.search(normalized, pageable).map(mapper::toResponse));
  }

  @Override
  @Transactional
  public UserAccountResponse changeRole(UUID userId, UserRole role) {
    var account = load(userId);
    account.changeRole(role);
    return mapper.toResponse(account);
  }

  @Override
  @Transactional
  public UserAccountResponse changeStatus(UUID userId, UserStatus status) {
    var account = load(userId);
    account.changeStatus(status);
    return mapper.toResponse(account);
  }

  @Override
  @Transactional
  public Optional<String> initiatePasswordReset(String email) {
    return repository.findByEmailIgnoreCase(email).map(account -> {
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

  private UserRole defaultRole() {
    return repository.count() == 0 ? UserRole.ADMIN : UserRole.RECEPTIONIST;
  }

  private UserAccount load(UUID userId) {
    return repository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
  }
}
