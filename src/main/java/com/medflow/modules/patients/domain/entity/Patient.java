package com.medflow.modules.patients.domain.entity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "patients", uniqueConstraints = @UniqueConstraint(name = "uk_patients_email", columnNames = "email"))
public class Patient {
  @Id private UUID id; @Column(nullable=false, length=100) private String firstName; @Column(nullable=false, length=100) private String lastName; @Column(nullable=false) private LocalDate dateOfBirth; @Column(nullable=false, length=10) private String gender; @Column(length=255) private String email; @Column(nullable=false, updatable=false) private Instant createdAt;
  protected Patient() { }
  public Patient(String firstName, String lastName, LocalDate dateOfBirth, String gender, String email) { this.id=UUID.randomUUID(); this.firstName=firstName; this.lastName=lastName; this.dateOfBirth=dateOfBirth; this.gender=gender; this.email=email; this.createdAt=Instant.now(); }
  public UUID getId(){return id;} public String getFirstName(){return firstName;} public String getLastName(){return lastName;} public LocalDate getDateOfBirth(){return dateOfBirth;} public String getGender(){return gender;} public String getEmail(){return email;} public Instant getCreatedAt(){return createdAt;}
}
