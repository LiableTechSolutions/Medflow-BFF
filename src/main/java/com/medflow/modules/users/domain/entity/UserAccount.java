package com.medflow.modules.users.domain.entity;

import com.medflow.shared.domain.AccountStatus;
import com.medflow.shared.domain.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A person who can sign in. {@code userUid} is a stable external identifier safe to share
 * with other systems, while the numeric id stays internal to the database.
 */
@Entity
@Table(name = "users")
public class UserAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "user_uid", nullable = false, length = 36)
  private String userUid;

  @Column(name = "role_id", nullable = false)
  private Integer roleId;

  @Column(name = "user_group_id")
  private Integer userGroupId;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", length = 100)
  private String lastName;

  @Column(nullable = false, length = 120)
  private String email;

  @Column(length = 20)
  private String phone;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(length = 10)
  private Gender gender;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(name = "profile_photo_url", length = 255)
  private String profilePhotoUrl;

  @Column(nullable = false, length = 30)
  private AccountStatus status;

  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @Column(name = "reset_token", length = 64)
  private String resetToken;

  @Column(name = "reset_token_expires_at")
  private Instant resetTokenExpiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "is_deleted", nullable = false)
  private boolean deleted;

  protected UserAccount() {
  }

  public UserAccount(Long hospitalId, Integer roleId, Integer userGroupId, String firstName,
      String lastName, String email, String phone, String passwordHash, Gender gender,
      LocalDate dateOfBirth) {
    this.hospitalId = hospitalId;
    this.userUid = UUID.randomUUID().toString();
    this.roleId = roleId;
    this.userGroupId = userGroupId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.passwordHash = passwordHash;
    this.gender = gender;
    this.dateOfBirth = dateOfBirth;
    this.status = AccountStatus.ACTIVE;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
    this.deleted = false;
  }

  public void updateProfile(String firstName, String lastName, String phone, Gender gender,
      LocalDate dateOfBirth, String profilePhotoUrl) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.phone = phone;
    this.gender = gender;
    this.dateOfBirth = dateOfBirth;
    this.profilePhotoUrl = profilePhotoUrl;
    touch();
  }

  public void changeRole(Integer roleId, Integer userGroupId) {
    this.roleId = roleId;
    if (userGroupId != null) {
      this.userGroupId = userGroupId;
    }
    touch();
  }

  public void changeStatus(AccountStatus status) {
    this.status = status;
    touch();
  }

  public void recordLogin(Instant when) {
    this.lastLoginAt = when;
    touch();
  }

  public void issueResetToken(String token, Instant expiresAt) {
    this.resetToken = token;
    this.resetTokenExpiresAt = expiresAt;
    touch();
  }

  public boolean isResetTokenExpired(Instant now) {
    return resetTokenExpiresAt == null || now.isAfter(resetTokenExpiresAt);
  }

  public void resetPassword(String newPasswordHash) {
    this.passwordHash = newPasswordHash;
    this.resetToken = null;
    this.resetTokenExpiresAt = null;
    touch();
  }

  public String getFullName() {
    return (lastName == null || lastName.isBlank()) ? firstName : firstName + " " + lastName;
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public String getUserUid() { return userUid; }
  public Integer getRoleId() { return roleId; }
  public Integer getUserGroupId() { return userGroupId; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public String getEmail() { return email; }
  public String getPhone() { return phone; }
  public String getPasswordHash() { return passwordHash; }
  public Gender getGender() { return gender; }
  public LocalDate getDateOfBirth() { return dateOfBirth; }
  public String getProfilePhotoUrl() { return profilePhotoUrl; }
  public AccountStatus getStatus() { return status; }
  public Instant getLastLoginAt() { return lastLoginAt; }
  public String getResetToken() { return resetToken; }
  public Instant getResetTokenExpiresAt() { return resetTokenExpiresAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public boolean isDeleted() { return deleted; }
}
