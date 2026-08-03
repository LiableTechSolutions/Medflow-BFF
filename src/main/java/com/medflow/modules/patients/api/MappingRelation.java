package com.medflow.modules.patients.api;

/** Mirrors {@code mapping_relation_enum}: how an account relates to a patient. */
public enum MappingRelation {
  SELF, PARENT, GUARDIAN, SPOUSE, CHILD, CARETAKER, OTHER
}
