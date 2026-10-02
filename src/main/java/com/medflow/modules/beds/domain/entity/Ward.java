package com.medflow.modules.beds.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "wards")
public class Ward {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false)
  private Long hospitalId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "ward_type", length = 40)
  private String wardType;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Ward() {
  }

  public Ward(Long hospitalId, String name, String wardType) {
    this.hospitalId = hospitalId;
    this.name = name;
    this.wardType = wardType;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public String getName() { return name; }
  public String getWardType() { return wardType; }
}
