package com.medflow.modules.doctors.domain.entity;

import com.medflow.shared.domain.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** Clinical profile of a doctor; personal details live on the linked user account. */
@Entity
@Table(name = "doctors")
public class Doctor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(name = "doctor_code", nullable = false, length = 30)
  private String doctorCode;

  @Column(nullable = false, length = 100)
  private String specialty;

  @Column(length = 200)
  private String qualification;

  @Column(name = "registration_number", nullable = false, length = 100)
  private String registrationNumber;

  @Column(name = "years_of_experience", nullable = false)
  private int yearsOfExperience;

  @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
  private BigDecimal consultationFee;

  @Column(name = "digital_signature_url", length = 255)
  private String digitalSignatureUrl;

  @Column(length = 2000)
  private String bio;

  @Column(nullable = false, length = 30)
  private AccountStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Doctor() {
  }

  public Doctor(Long hospitalId, Long userId, String doctorCode, String specialty,
      String qualification, String registrationNumber, Integer yearsOfExperience,
      BigDecimal consultationFee, String bio) {
    this.hospitalId = hospitalId;
    this.userId = userId;
    this.doctorCode = doctorCode;
    this.specialty = specialty;
    this.qualification = qualification;
    this.registrationNumber = registrationNumber;
    this.yearsOfExperience = yearsOfExperience == null ? 0 : yearsOfExperience;
    this.consultationFee = consultationFee;
    this.bio = bio;
    this.status = AccountStatus.ACTIVE;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void update(String specialty, String qualification, Integer yearsOfExperience,
      BigDecimal consultationFee, String digitalSignatureUrl, String bio) {
    this.specialty = specialty;
    this.qualification = qualification;
    this.yearsOfExperience = yearsOfExperience == null ? this.yearsOfExperience : yearsOfExperience;
    this.consultationFee = consultationFee;
    this.digitalSignatureUrl = digitalSignatureUrl;
    this.bio = bio;
    touch();
  }

  public void changeStatus(AccountStatus status) {
    this.status = status;
    touch();
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public Long getHospitalId() { return hospitalId; }
  public String getDoctorCode() { return doctorCode; }
  public String getSpecialty() { return specialty; }
  public String getQualification() { return qualification; }
  public String getRegistrationNumber() { return registrationNumber; }
  public int getYearsOfExperience() { return yearsOfExperience; }
  public BigDecimal getConsultationFee() { return consultationFee; }
  public String getDigitalSignatureUrl() { return digitalSignatureUrl; }
  public String getBio() { return bio; }
  public AccountStatus getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
