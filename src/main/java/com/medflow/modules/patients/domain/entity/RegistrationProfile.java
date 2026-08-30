package com.medflow.modules.patients.domain.entity;

import com.medflow.modules.patients.api.RegistrationFieldState;
import com.medflow.modules.patients.api.RegistrationProfileType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "patient_registration_profiles")
public class RegistrationProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hospital_id", nullable = false, unique = true)
  private Long hospitalId;

  @Enumerated(EnumType.STRING)
  @Column(name = "starting_profile", nullable = false, length = 20)
  private RegistrationProfileType startingProfile;

  @Column(name = "field_states_json", nullable = false, length = 4000)
  private String fieldStatesJson;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected RegistrationProfile() {
  }

  public RegistrationProfile(Long hospitalId, RegistrationProfileType startingProfile,
      Map<String, RegistrationFieldState> fieldStates) {
    this.hospitalId = hospitalId;
    this.startingProfile = startingProfile;
    this.fieldStatesJson = serialize(fieldStates);
    this.updatedAt = Instant.now();
  }

  public void update(RegistrationProfileType startingProfile, Map<String, RegistrationFieldState> fieldStates) {
    this.startingProfile = startingProfile;
    this.fieldStatesJson = serialize(fieldStates);
    this.updatedAt = Instant.now();
  }

  public Map<String, RegistrationFieldState> fieldStates() {
    return deserialize(fieldStatesJson);
  }

  public Long getId() { return id; }
  public Long getHospitalId() { return hospitalId; }
  public RegistrationProfileType getStartingProfile() { return startingProfile; }
  public String getFieldStatesJson() { return fieldStatesJson; }
  public Instant getUpdatedAt() { return updatedAt; }

  private static String serialize(Map<String, RegistrationFieldState> fieldStates) {
    var normalized = new LinkedHashMap<String, RegistrationFieldState>();
    fieldStates.forEach((field, state) -> {
      if (field != null && !field.isBlank() && state != null) {
        normalized.put(field, state);
      }
    });
    var sb = new StringBuilder();
    var entries = normalized.entrySet().iterator();
    while (entries.hasNext()) {
      var entry = entries.next();
      sb.append(entry.getKey()).append('=').append(entry.getValue().name());
      if (entries.hasNext()) {
        sb.append(';');
      }
    }
    return sb.toString();
  }

  private static Map<String, RegistrationFieldState> deserialize(String json) {
    var map = new LinkedHashMap<String, RegistrationFieldState>();
    if (json == null || json.isBlank()) {
      return map;
    }
    for (String pair : json.split(";")) {
      if (pair.isBlank()) {
        continue;
      }
      var idx = pair.indexOf('=');
      if (idx < 0) {
        continue;
      }
      var key = pair.substring(0, idx).trim();
      var value = pair.substring(idx + 1).trim();
      if (!key.isEmpty()) {
        map.put(key, RegistrationFieldState.from(value));
      }
    }
    return map;
  }
}
