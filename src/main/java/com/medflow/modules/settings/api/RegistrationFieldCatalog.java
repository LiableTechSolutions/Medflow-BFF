package com.medflow.modules.settings.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class RegistrationFieldCatalog {
  private RegistrationFieldCatalog() {}

  public static final Set<String> FIELDS = Set.of(
      "first_name", "middle_name", "last_name", "date_of_birth", "gender", "biological_sex",
      "marital_status", "preferred_language", "contact_number", "alternate_number", "email",
      "address_line_1", "address_line_2", "city", "state", "postal_code", "country",
      "emergency_contact_name", "emergency_contact_relation", "emergency_contact_phone",
      "insurance_provider", "policy_number", "group_number", "primary_cardholder_name",
      "primary_physician_id", "referring_doctor", "allergies_summary", "current_medications",
      "clinical_notes");

  public static Map<String, RegistrationFieldState> basic() {
    var states = new LinkedHashMap<String, RegistrationFieldState>();
    FIELDS.forEach(field -> states.put(field, RegistrationFieldState.HIDDEN));
    states.put("first_name", RegistrationFieldState.REQUIRED);
    states.put("last_name", RegistrationFieldState.REQUIRED);
    states.put("date_of_birth", RegistrationFieldState.REQUIRED);
    states.put("contact_number", RegistrationFieldState.OPTIONAL);
    states.put("email", RegistrationFieldState.OPTIONAL);
    return states;
  }

  public static Map<String, RegistrationFieldState> comprehensive() {
    var states = new LinkedHashMap<String, RegistrationFieldState>();
    FIELDS.forEach(field -> states.put(field, RegistrationFieldState.OPTIONAL));
    states.put("first_name", RegistrationFieldState.REQUIRED);
    states.put("last_name", RegistrationFieldState.REQUIRED);
    states.put("date_of_birth", RegistrationFieldState.REQUIRED);
    states.put("contact_number", RegistrationFieldState.REQUIRED);
    return states;
  }
}
