package com.medflow.modules.auth.application;

import com.medflow.modules.auth.api.AuthService;
import com.medflow.modules.auth.api.request.LoginRequest;
import com.medflow.modules.auth.api.request.RegisterRequest;
import com.medflow.modules.auth.api.request.ResetPasswordRequest;
import com.medflow.modules.auth.api.response.AuthResponse;
import com.medflow.modules.rbac.api.AccessControlService;
import com.medflow.modules.rbac.api.GroupCodes;
import com.medflow.modules.rbac.api.RoleCodes;
import com.medflow.modules.tenancy.api.HospitalService;
import com.medflow.modules.tenancy.api.request.RegisterHospitalRequest;
import com.medflow.modules.users.api.UserAccountService;
import com.medflow.modules.users.api.request.CreateUserAccountRequest;
import com.medflow.modules.users.api.response.UserAccountResponse;
import com.medflow.shared.exception.BusinessRuleViolationException;
import com.medflow.shared.exception.DuplicateResourceException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AuthServiceImpl implements AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

  private final HospitalService hospitalService;
  private final UserAccountService userAccountService;
  private final AccessControlService accessControl;
  private final TokenService tokenService;

  AuthServiceImpl(HospitalService hospitalService, UserAccountService userAccountService,
      AccessControlService accessControl, TokenService tokenService) {
    this.hospitalService = hospitalService;
    this.userAccountService = userAccountService;
    this.accessControl = accessControl;
    this.tokenService = tokenService;
  }

  /**
   * Sign-up provisions a whole workspace in one transaction: the hospital, its module
   * entitlements and the administrator account. If any step fails nothing is left behind.
   */
  @Override
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (!request.password().equals(request.confirmPassword())) {
      throw new BusinessRuleViolationException("Passwords do not match");
    }
    if (userAccountService.emailExists(request.email())) {
      throw new DuplicateResourceException("A user with this email already exists");
    }

    var name = splitName(request.fullName());
    var hospital = hospitalService.register(new RegisterHospitalRequest(
        workspaceName(request, name.first()), null, null, null, null, null, null, null,
        request.phone(), request.email(), null));

    var admin = userAccountService.create(hospital.id(), new CreateUserAccountRequest(
        name.first(), name.last(), request.email(), request.phone(), request.password(),
        RoleCodes.ADMIN, GroupCodes.STAFF_PORTAL, null, null));

    return toAuthResponse(admin);
  }

  @Override
  @Transactional
  public AuthResponse login(LoginRequest request) {
    var user = userAccountService.verifyCredentials(request.email(), request.password());
    userAccountService.recordLogin(user.id());
    return toAuthResponse(user);
  }

  @Override
  public void requestPasswordReset(String email) {
    userAccountService.initiatePasswordReset(email).ifPresent(token ->
        // Placeholder delivery channel until an email adapter is wired in; the token is
        // never returned over the API, to avoid account enumeration.
        log.info("Password reset token issued for {}: {}", email, token));
  }

  @Override
  public void resetPassword(ResetPasswordRequest request) {
    userAccountService.resetPassword(request.token(), request.newPassword());
  }

  @Override
  public UserAccountResponse currentUser(Long userId) {
    return userAccountService.getById(userId);
  }

  private AuthResponse toAuthResponse(UserAccountResponse user) {
    List<String> permissions = accessControl.permissionCodes(user.roleId(), user.hospitalId());
    var token = tokenService.issue(user, permissions);
    return AuthResponse.bearer(token.value(), token.expiresAt(), user, permissions);
  }

  private String workspaceName(RegisterRequest request, String firstName) {
    if (request.hospitalName() != null && !request.hospitalName().isBlank()) {
      return request.hospitalName().trim();
    }
    return firstName + "'s Clinic";
  }

  private PersonName splitName(String fullName) {
    var trimmed = fullName.trim();
    var separator = trimmed.lastIndexOf(' ');
    return separator < 0
        ? new PersonName(trimmed, null)
        : new PersonName(trimmed.substring(0, separator), trimmed.substring(separator + 1));
  }

  private record PersonName(String first, String last) { }
}
