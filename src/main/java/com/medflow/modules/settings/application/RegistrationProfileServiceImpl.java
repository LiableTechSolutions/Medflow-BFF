package com.medflow.modules.settings.application;

import com.medflow.modules.audit.api.AuditService;
import com.medflow.modules.settings.api.RegistrationFieldCatalog;
import com.medflow.modules.settings.api.RegistrationFieldState;
import com.medflow.modules.settings.api.RegistrationProfileService;
import com.medflow.modules.settings.api.request.UpdateRegistrationProfileRequest;
import com.medflow.modules.settings.api.response.RegistrationProfileResponse;
import com.medflow.modules.settings.domain.entity.HospitalRegistrationProfile;
import com.medflow.modules.settings.domain.repository.HospitalRegistrationProfileRepository;
import com.medflow.shared.exception.BusinessRuleViolationException;
import com.medflow.shared.api.ApiError;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.MDC;

@Service
class RegistrationProfileServiceImpl implements RegistrationProfileService {
  private final HospitalRegistrationProfileRepository repository;
  private final AuditService auditService;

  RegistrationProfileServiceImpl(HospitalRegistrationProfileRepository repository,
      AuditService auditService) {
    this.repository = repository;
    this.auditService = auditService;
  }

  @Override
  @Transactional(readOnly = true)
  public RegistrationProfileResponse get(Long hospitalId) {
    return toResponse(repository.findByHospitalId(hospitalId).orElseGet(
        () -> new HospitalRegistrationProfile(hospitalId, "BASIC", RegistrationFieldCatalog.basic(), null)));
  }

  @Override
  @Transactional
  public RegistrationProfileResponse update(Long hospitalId, Long actorId,
      UpdateRegistrationProfileRequest request) {
    var states = new LinkedHashMap<>(request.fieldStates());
    try {
      validate(request.template(), states);
    } catch (BusinessRuleViolationException exception) {
      auditService.record("PATIENT_REGISTRATION_PROFILE_UPDATE_REJECTED", "hospital_registration_profile",
          hospitalId, "{\"outcome\":\"REJECTED\",\"reason\":\"policy_validation\"}");
      throw exception;
    }
    var existing = repository.findByHospitalId(hospitalId).orElseGet(
        () -> new HospitalRegistrationProfile(hospitalId, request.template(), states, actorId));
    if (existing.getHospitalId().equals(hospitalId) && existing.getFieldStates() != null) {
      existing.update(request.template(), states, actorId);
    }
    var saved = repository.save(existing);
    auditService.record("PATIENT_REGISTRATION_PROFILE_UPDATED", "hospital_registration_profile",
        hospitalId, auditMetadata(request.template(), states));
    return toResponse(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, RegistrationFieldState> statesFor(Long hospitalId) {
    return new LinkedHashMap<>(get(hospitalId).fieldStates());
  }

  private void validate(String template, Map<String, RegistrationFieldState> states) {
    if (!"BASIC".equals(template) && !"COMPREHENSIVE".equals(template)) {
      throw violation("template", "template must be BASIC or COMPREHENSIVE");
    }
    if (!states.keySet().equals(RegistrationFieldCatalog.FIELDS)) {
      throw violation("fieldStates", "fieldStates must contain exactly the supported registration fields");
    }
    for (var field : RegistrationFieldCatalog.FIELDS) {
      if (states.get(field) == null) {
        throw violation(field, "Every supported registration field must have a state");
      }
    }
    if (states.get("first_name") == RegistrationFieldState.HIDDEN
        || states.get("last_name") == RegistrationFieldState.HIDDEN
        || states.get("date_of_birth") == RegistrationFieldState.HIDDEN) {
      throw violation("baseline_fields", "first_name, last_name, and date_of_birth cannot be hidden");
    }
    if (states.get("contact_number") == RegistrationFieldState.HIDDEN
        && states.get("email") == RegistrationFieldState.HIDDEN) {
      throw violation("contact_methods", "At least one reachable contact method, contact_number or email, must remain visible");
    }
  }

  private BusinessRuleViolationException violation(String field, String message) {
    return new BusinessRuleViolationException(message,
        java.util.List.of(new ApiError(field, "REGISTRATION_POLICY_ERROR", message)));
  }

  private String auditMetadata(String template, Map<String, RegistrationFieldState> states) {
    var changed = states.entrySet().stream()
      .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
      .collect(java.util.stream.Collectors.joining(",", "{", "}"));
    return "{\"outcome\":\"SUCCESS\",\"correlationId\":\""
      + String.valueOf(MDC.get("traceId")) + "\",\"template\":\"" + template
      + "\",\"fieldStates\":" + changed + "}";
  }

  private RegistrationProfileResponse toResponse(HospitalRegistrationProfile profile) {
    return new RegistrationProfileResponse(profile.getTemplate(),
        new LinkedHashMap<>(profile.getFieldStates()), profile.getUpdatedAt());
  }
}
