package com.medflow.modules.patients.api;

import com.medflow.modules.patients.api.request.AddMedicalHistoryRequest;
import com.medflow.modules.patients.api.request.AddPatientReportRequest;
import com.medflow.modules.patients.api.request.AdmitPatientRequest;
import com.medflow.modules.patients.api.request.CreateDailyAnalysisRequest;
import com.medflow.modules.patients.api.request.CreatePatientRequest;
import com.medflow.modules.patients.api.request.DischargePatientRequest;
import com.medflow.modules.patients.api.request.LinkPatientAccountRequest;
import com.medflow.modules.patients.api.request.UpdatePatientRequest;
import com.medflow.modules.patients.api.response.DailyAnalysisResponse;
import com.medflow.modules.patients.api.response.HospitalisationRecordResponse;
import com.medflow.modules.patients.api.response.MedicalHistoryResponse;
import com.medflow.modules.patients.api.response.PatientAccountResponse;
import com.medflow.modules.patients.api.response.PatientClinicalSummaryResponse;
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

  /** Admits a patient: creates the hospitalisation record and flips {@code isHospitalised}. */
  HospitalisationRecordResponse admitPatient(Long hospitalId, Long patientId,
      AdmitPatientRequest request);

  /** Discharges the patient's active stay; the record is kept, not deleted. */
  HospitalisationRecordResponse dischargePatient(Long hospitalId, Long patientId,
      DischargePatientRequest request);

  /** Doctor-only: records a vitals/notes entry against the active hospitalisation record. */
  DailyAnalysisResponse addDailyAnalysis(Long hospitalId, Long patientId,
      CreateDailyAnalysisRequest request);

  /** All daily analyses for the patient across their hospitalisation history, oldest first. */
  List<DailyAnalysisResponse> dailyAnalyses(Long hospitalId, Long patientId);

  /** Core profile plus the current/most recent hospitalisation stay and its daily analyses. */
  PatientClinicalSummaryResponse summary(Long hospitalId, Long patientId);
}
