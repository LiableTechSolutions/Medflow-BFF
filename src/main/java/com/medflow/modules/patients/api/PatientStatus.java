package com.medflow.modules.patients.api;

/** Records are never deleted; inactive patients are archived instead. */
public enum PatientStatus {
  ACTIVE, INACTIVE
}
