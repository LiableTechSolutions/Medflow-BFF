package com.medflow.modules.settings.domain.entity;

import com.medflow.modules.settings.api.RegistrationFieldState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "hospital_registration_profiles")
public class HospitalRegistrationProfile {
  @Id
  @Column(name = "hospital_id")
  private Long hospitalId;

  @Column(nullable = false, length = 30)
  private String template;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "field_states", nullable = false, columnDefinition = "jsonb")
  private Map<String, RegistrationFieldState> fieldStates;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "updated_by")
  private Long updatedBy;

  protected HospitalRegistrationProfile() {}

  public HospitalRegistrationProfile(Long hospitalId, String template,
      Map<String, RegistrationFieldState> fieldStates, Long updatedBy) {
    this.hospitalId = hospitalId;
    this.template = template;
    this.fieldStates = fieldStates;
    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;
  }

  public void update(String template, Map<String, RegistrationFieldState> fieldStates, Long updatedBy) {
    this.template = template;
    this.fieldStates = fieldStates;
    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;
  }

  public Long getHospitalId() { return hospitalId; }
  public String getTemplate() { return template; }
  public Map<String, RegistrationFieldState> getFieldStates() { return fieldStates; }
  public Instant getUpdatedAt() { return updatedAt; }
}
