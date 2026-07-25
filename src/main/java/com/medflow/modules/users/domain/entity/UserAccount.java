package com.medflow.modules.users.domain.entity;

import com.medflow.modules.users.api.UserRole;
import com.medflow.modules.users.api.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users",
    uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class UserAccount {

  @Id
  private UUID id;

  @Column(nullable = false, length = 150)
  private String fullName;

  @Column(nullable = false, length = 255)
  private String email;

  @Column(nullable = false, length = 100)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserStatus status;

  @Column(length = 64)
  private String resetToken;

  private Instant resetTokenExpiresAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected UserAccount() {
  }

  public UserAccount(String fullName, String email, String passwordHash, UserRole role) {
    this.id = UUID.randomUUID();
    this.fullName = fullName;
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
    this.status = UserStatus.ACTIVE;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void changeRole(UserRole role) {
    this.role = role;
    touch();
  }

  public void changeStatus(UserStatus status) {
    this.status = status;
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

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public String getFullName() { return fullName; }
  public String getEmail() { return email; }
  public String getPasswordHash() { return passwordHash; }
  public UserRole getRole() { return role; }
  public UserStatus getStatus() { return status; }
  public String getResetToken() { return resetToken; }
  public Instant getResetTokenExpiresAt() { return resetTokenExpiresAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
