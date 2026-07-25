package com.medflow.modules.patients.domain.entity;

import com.medflow.modules.patients.api.PatientStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patients",
    uniqueConstraints = @UniqueConstraint(name = "uk_patients_email", columnNames = "email"))
public class Patient {

  @Id
  private UUID id;

  @Column(nullable = false, length = 100)
  private String firstName;

  @Column(nullable = false, length = 100)
  private String lastName;

  @Column(nullable = false)
  private LocalDate dateOfBirth;

  @Column(nullable = false, length = 10)
  private String gender;

  @Column(length = 255)
  private String email;

  @Column(length = 20)
  private String phone;

  @Column(length = 5)
  private String bloodGroup;

  @Column(length = 255)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PatientStatus status;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected Patient() {
  }

  public Patient(String firstName, String lastName, LocalDate dateOfBirth, String gender,
      String email, String phone, String bloodGroup, String address) {
    this.id = UUID.randomUUID();
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
    this.email = email;
    this.phone = phone;
    this.bloodGroup = bloodGroup;
    this.address = address;
    this.status = PatientStatus.ACTIVE;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void update(String firstName, String lastName, LocalDate dateOfBirth, String gender,
      String email, String phone, String bloodGroup, String address, PatientStatus status) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
    this.email = email;
    this.phone = phone;
    this.bloodGroup = bloodGroup;
    this.address = address;
    this.status = status;
    this.updatedAt = Instant.now();
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }

  public UUID getId() { return id; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public LocalDate getDateOfBirth() { return dateOfBirth; }
  public String getGender() { return gender; }
  public String getEmail() { return email; }
  public String getPhone() { return phone; }
  public String getBloodGroup() { return bloodGroup; }
  public String getAddress() { return address; }
  public PatientStatus getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
