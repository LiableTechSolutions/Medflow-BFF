package com.medflow.modules.tenancy.domain.entity;

import com.medflow.modules.tenancy.api.request.UpdateHospitalRequest;
import com.medflow.shared.domain.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A tenant. Every clinical record in the system belongs to exactly one hospital. */
@Entity
@Table(name = "hospitals")
public class Hospital {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_code", nullable = false, length = 20)
  private String hospitalCode;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(name = "legal_name", length = 200)
  private String legalName;

  @Column(name = "hospital_type", length = 50)
  private String hospitalType;

  @Column(name = "address_line1", length = 200)
  private String addressLine1;

  @Column(name = "address_line2", length = 200)
  private String addressLine2;

  @Column(length = 100)
  private String city;

  @Column(length = 100)
  private String state;

  @Column(length = 100)
  private String country;

  @Column(length = 10)
  private String pincode;

  @Column(length = 20)
  private String phone;

  @Column(length = 120)
  private String email;

  @Column(nullable = false, length = 50)
  private String timezone;

  @Column(nullable = false, length = 30)
  private AccountStatus status;

  @Column(name = "onboarded_at")
  private Instant onboardedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "is_deleted", nullable = false)
  private boolean deleted;

  protected Hospital() {
  }

  public Hospital(String hospitalCode, String name, String legalName, String hospitalType,
      String addressLine1, String city, String state, String country, String pincode,
      String phone, String email, String timezone) {
    this.hospitalCode = hospitalCode;
    this.name = name;
    this.legalName = legalName;
    this.hospitalType = hospitalType;
    this.addressLine1 = addressLine1;
    this.city = city;
    this.state = state;
    this.country = country;
    this.pincode = pincode;
    this.phone = phone;
    this.email = email;
    this.timezone = timezone;
    this.status = AccountStatus.ACTIVE;
    this.onboardedAt = Instant.now();
    this.createdAt = this.onboardedAt;
    this.updatedAt = this.onboardedAt;
    this.deleted = false;
  }

  public void update(UpdateHospitalRequest request) {
    this.name = request.name();
    this.legalName = request.legalName();
    this.hospitalType = request.hospitalType();
    this.addressLine1 = request.addressLine1();
    this.addressLine2 = request.addressLine2();
    this.city = request.city();
    this.state = request.state();
    this.country = request.country();
    this.pincode = request.pincode();
    this.phone = request.phone();
    this.email = request.email();
    if (request.timezone() != null && !request.timezone().isBlank()) {
      this.timezone = request.timezone();
    }
    this.updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public String getHospitalCode() { return hospitalCode; }
  public String getName() { return name; }
  public String getLegalName() { return legalName; }
  public String getHospitalType() { return hospitalType; }
  public String getAddressLine1() { return addressLine1; }
  public String getAddressLine2() { return addressLine2; }
  public String getCity() { return city; }
  public String getState() { return state; }
  public String getCountry() { return country; }
  public String getPincode() { return pincode; }
  public String getPhone() { return phone; }
  public String getEmail() { return email; }
  public String getTimezone() { return timezone; }
  public AccountStatus getStatus() { return status; }
  public Instant getOnboardedAt() { return onboardedAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public boolean isDeleted() { return deleted; }
}
