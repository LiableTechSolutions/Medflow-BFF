package com.medflow.modules.patients.api;

import com.medflow.modules.patients.api.request.CreatePatientRequest;
import com.medflow.modules.patients.api.request.UpdatePatientRequest;
import com.medflow.modules.patients.api.response.PatientResponse;
import com.medflow.shared.api.PageResponse;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Public API of the Patients module. */
public interface PatientService {

  PatientResponse create(CreatePatientRequest request);

  PatientResponse findById(UUID patientId);

  PatientResponse update(UUID patientId, UpdatePatientRequest request);

  PageResponse<PatientResponse> search(String query, int page, int size);

  /** Batch name lookup for cross-module display (appointments, prescriptions, laboratory). */
  List<PatientSummary> summariesByIds(Collection<UUID> patientIds);

  long count();
}
