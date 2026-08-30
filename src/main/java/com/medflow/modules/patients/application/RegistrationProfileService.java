package com.medflow.modules.patients.application;

import com.medflow.modules.audit.api.AuditService;
import com.medflow.modules.patients.api.RegistrationFieldState;
import com.medflow.modules.patients.api.RegistrationProfileType;
import com.medflow.modules.patients.api.request.UpdateRegistrationProfileRequest;
import com.medflow.modules.patients.api.response.RegistrationProfileResponse;
import com.medflow.modules.patients.domain.entity.RegistrationProfile;
import com.medflow.modules.patients.domain.repository.RegistrationProfileRepository;
import com.medflow.shared.exception.ProfileValidationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationProfileService {

  private static final List<String> FIELD_ORDER = List.of(
      "firstName",
      "lastName",
      "gender",
      "dateOfBirth",
      "bloodGroup",
      "phone",
      "email",
      "address",
      "emergencyContactName",
      "emergencyContactPhone");

  private final RegistrationProfileRepository repository;
  private final AuditService auditService;

  public RegistrationProfileService(RegistrationProfileRepository repository, AuditService auditService) {
    this.repository = repository;
    this.auditService = auditService;
  }

  @Transactional(readOnly = true)
  public RegistrationProfileResponse get(Long hospitalId) {
    var profile = repository.findByHospitalId(hospitalId)
        .orElseGet(() -> repository.save(new RegistrationProfile(hospitalId,
            RegistrationProfileType.BASIC,
            defaultFieldStates(RegistrationProfileType.BASIC))));
    auditService.record("PROFILE_RETRIEVED", "RegistrationProfile", hospitalId, null);
    return toResponse(profile);
  }

  @Transactional(readOnly = true)
  public Map<String, RegistrationFieldState> getFieldStates(Long hospitalId) {
    return repository.findByHospitalId(hospitalId)
        .map(RegistrationProfile::fieldStates)
        .orElseGet(() -> defaultFieldStates(RegistrationProfileType.BASIC));
  }

  @Transactional(readOnly = true)
  public void validatePatientFields(Long hospitalId, Map<String, String> values) {
    var profile = repository.findByHospitalId(hospitalId)
        .map(RegistrationProfile::fieldStates)
        .orElseGet(() -> defaultFieldStates(RegistrationProfileType.BASIC));
    var errors = new java.util.ArrayList<com.medflow.shared.api.ApiError>();
    for (String field : FIELD_ORDER) {
      var state = profile.getOrDefault(field, RegistrationFieldState.OPTIONAL);
      var value = values.get(field);
      var isBlank = value == null || value.isBlank();
      if (state == RegistrationFieldState.REQUIRED && isBlank) {
        errors.add(new com.medflow.shared.api.ApiError(field, "REQUIRED_FIELD",
            "This field is required for this hospital profile."));
      }
      if (state == RegistrationFieldState.HIDDEN && !isBlank) {
        errors.add(new com.medflow.shared.api.ApiError(field, "HIDDEN_FIELD",
            "This field is hidden for this hospital profile."));
      }
    }
    if (!errors.isEmpty()) {
      throw new ProfileValidationException("Patient registration profile validation failed", errors);
    }
  }

  @Transactional
  public RegistrationProfileResponse update(Long hospitalId, UpdateRegistrationProfileRequest request) {
    var sanitized = sanitizeFieldStates(request.fieldStates());
    validateProfile(request.startingProfile(), sanitized);

    var profile = repository.findByHospitalId(hospitalId)
        .map(existing -> {
          existing.update(request.startingProfile(), sanitized);
          return existing;
        })
        .orElseGet(() -> repository.save(new RegistrationProfile(hospitalId,
            request.startingProfile(), sanitized)));
    
    var response = toResponse(profile);
    auditService.record("PROFILE_UPDATED", "RegistrationProfile", profile.getId(), profile.getFieldStatesJson());
    return response;
  }

  public Map<String, RegistrationFieldState> defaultFieldStates(RegistrationProfileType type) {
    var defaults = new LinkedHashMap<String, RegistrationFieldState>();
    for (String field : FIELD_ORDER) {
      defaults.put(field, switch (type) {
        case BASIC -> switch (field) {
          case "firstName", "email" -> RegistrationFieldState.REQUIRED;
          case "lastName", "gender", "dateOfBirth", "bloodGroup", "phone", "address",
              "emergencyContactName", "emergencyContactPhone" -> RegistrationFieldState.OPTIONAL;
          default -> RegistrationFieldState.OPTIONAL;
        };
        case COMPREHENSIVE -> RegistrationFieldState.REQUIRED;
      });
    }
    return defaults;
  }

  private Map<String, RegistrationFieldState> sanitizeFieldStates(Map<String, String> raw) {
    var normalized = new LinkedHashMap<String, RegistrationFieldState>();
    if (raw == null || raw.isEmpty()) {
      return defaultFieldStates(RegistrationProfileType.BASIC);
    }
    for (String field : FIELD_ORDER) {
      var state = raw.get(field);
      if (state == null) {
        normalized.put(field, RegistrationFieldState.OPTIONAL);
        continue;
      }
      normalized.put(field, RegistrationFieldState.from(state));
    }
    return normalized;
  }

  private void validateProfile(RegistrationProfileType type, Map<String, RegistrationFieldState> fieldStates) {
    if (type == null || fieldStates == null || fieldStates.isEmpty()) {
      throw new ProfileValidationException("Registration profile is invalid",
          List.of(new com.medflow.shared.api.ApiError("startingProfile", "INVALID_PROFILE",
              "A starting profile is required.")));
    }

    var errors = new java.util.ArrayList<com.medflow.shared.api.ApiError>();
    var requiredCount = 0;
    
    for (String field : FIELD_ORDER) {
      var state = fieldStates.get(field);
      if (state == null) {
        errors.add(new com.medflow.shared.api.ApiError(field, "INVALID_STATE",
            "Field state is required."));
        continue;
      }
      if (state == RegistrationFieldState.REQUIRED) {
        requiredCount++;
      }
      
      // Safety rule: Cannot mark firstName or email as HIDDEN in BASIC profile
      if (type == RegistrationProfileType.BASIC && state == RegistrationFieldState.HIDDEN) {
        if ("firstName".equals(field) || "email".equals(field)) {
          errors.add(new com.medflow.shared.api.ApiError(field, "INVALID_STATE",
              field + " cannot be hidden in BASIC profile (required for patient registration)."));
        }
      }
    }

    // Validate all required fields are present in the map
    for (String expectedField : FIELD_ORDER) {
      if (!fieldStates.containsKey(expectedField)) {
        errors.add(new com.medflow.shared.api.ApiError(expectedField, "MISSING_FIELD",
            "Field configuration is missing."));
      }
    }

    // Safety rule: At least one REQUIRED field must exist
    if (requiredCount == 0) {
      errors.add(new com.medflow.shared.api.ApiError("fieldStates", "INVALID_PROFILE",
          "Profile must have at least one required field."));
    }

    // Safety rule: BASIC profile can only require up to 2 mandatory fields
    if (type == RegistrationProfileType.BASIC && requiredCount > 2) {
      errors.add(new com.medflow.shared.api.ApiError("startingProfile", "INVALID_PROFILE",
          "Basic profile can only require up to 2 mandatory fields."));
    }

    // Safety rule: COMPREHENSIVE profile must have at least 5 REQUIRED fields
    if (type == RegistrationProfileType.COMPREHENSIVE && requiredCount < 5) {
      errors.add(new com.medflow.shared.api.ApiError("startingProfile", "INVALID_PROFILE",
          "Comprehensive profile must require at least 5 mandatory fields."));
    }

    if (!errors.isEmpty()) {
      throw new ProfileValidationException("Registration profile validation failed", errors);
    }
  }

  private RegistrationProfileResponse toResponse(RegistrationProfile profile) {
    var states = new LinkedHashMap<String, String>();
    profile.fieldStates().forEach((field, state) -> states.put(field, state.name()));
    return new RegistrationProfileResponse(profile.getId(), profile.getHospitalId(),
        profile.getStartingProfile(), states, profile.getUpdatedAt());
  }
}
