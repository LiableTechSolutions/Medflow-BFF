package com.medflow.modules.patients.api;

import com.medflow.modules.patients.api.request.AddMedicalHistoryRequest;
import com.medflow.modules.patients.api.request.AddPatientReportRequest;
import com.medflow.modules.patients.api.request.CreatePatientRequest;
import com.medflow.modules.patients.api.request.LinkPatientAccountRequest;
import com.medflow.modules.patients.api.request.UpdatePatientRequest;
import com.medflow.modules.patients.api.response.MedicalHistoryResponse;
import com.medflow.modules.patients.api.response.PatientAccountResponse;
import com.medflow.modules.patients.api.response.PatientReportResponse;
import com.medflow.modules.patients.api.response.PatientResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.domain.AccountStatus;
import java.util.Collection;
import java.util.List;

/** Public API of the Patients module. */
public interface PatientService {

  PatientResponse create(Long hospitalId, CreatePatientRequest request);

  PatientResponse findById(Long hospitalId, Long patientId);

  PatientResponse update(Long hospitalId, Long patientId, UpdatePatientRequest request);

  PageResponse<PatientResponse> search(Long hospitalId, String query, AccountStatus status,
      int page, int size);

  List<MedicalHistoryResponse> medicalHistory(Long hospitalId, Long patientId);

  MedicalHistoryResponse addMedicalHistory(Long hospitalId, Long patientId,
      AddMedicalHistoryRequest request);

  List<PatientReportResponse> reports(Long hospitalId, Long patientId);

  PatientReportResponse addReport(Long hospitalId, Long patientId, Long uploadedByUserId,
      AddPatientReportRequest request);

  List<PatientAccountResponse> linkedAccounts(Long hospitalId, Long patientId);

  /** Lets a portal account (the patient themselves, a parent, a caretaker) act for a patient. */
  PatientAccountResponse linkAccount(Long hospitalId, Long patientId,
      LinkPatientAccountRequest request);

  /** Batch name lookup for cross-module display (appointments, prescriptions, laboratory). */
  List<PatientSummary> summariesByIds(Long hospitalId, Collection<Long> patientIds);

  long countByHospital(Long hospitalId);
}
