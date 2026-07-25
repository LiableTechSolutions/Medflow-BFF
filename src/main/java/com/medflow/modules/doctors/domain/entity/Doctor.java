package com.medflow.modules.doctors.domain.entity;

import com.medflow.modules.doctors.api.DoctorAvailability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doctors", uniqueConstraints = {
    @UniqueConstraint(name = "uk_doctors_email", columnNames = "email"),
    @UniqueConstraint(name = "uk_doctors_license", columnNames = "license_number")})
public class Doctor {

  @Id
  private UUID id;

  @Column(nullable = false, length = 150)
  private String fullName;

  @Column(nullable = false, length = 255)
  private String email;

  @Column(length = 20)
  private String phone;

  @Column(nullable = false, length = 100)
  private String specialty;

  @Column(nullable = false, length = 100)
  private String department;

  @Column(name = "license_number", nullable = false, length = 50)
  private String licenseNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DoctorAvailability availability;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal consultationFee;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected Doctor() {
  }

  public Doctor(String fullName, String email, String phone, String specialty, String department,
      String licenseNumber, BigDecimal consultationFee) {
    this.id = UUID.randomUUID();
    this.fullName = fullName;
    this.email = email;
    this.phone = phone;
    this.specialty = specialty;
    this.department = department;
    this.licenseNumber = licenseNumber;
    this.consultationFee = consultationFee;
    this.availability = DoctorAvailability.AVAILABLE;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void update(String fullName, String email, String phone, String specialty,
      String department, BigDecimal consultationFee) {
    this.fullName = fullName;
    this.email = email;
    this.phone = phone;
    this.specialty = specialty;
    this.department = department;
    this.consultationFee = consultationFee;
    touch();
  }

  public void changeAvailability(DoctorAvailability availability) {
    this.availability = availability;
    touch();
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public String getFullName() { return fullName; }
  public String getEmail() { return email; }
  public String getPhone() { return phone; }
  public String getSpecialty() { return specialty; }
  public String getDepartment() { return department; }
  public String getLicenseNumber() { return licenseNumber; }
  public DoctorAvailability getAvailability() { return availability; }
  public BigDecimal getConsultationFee() { return consultationFee; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
