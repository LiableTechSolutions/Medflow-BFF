package com.medflow.modules.patients.api.response;

import java.util.List;

/**
 * One-call read for rendering either the OPD or the inpatient view: the patient's core
 * profile, whether they're currently hospitalised, their current/most recent
 * {@link HospitalisationRecordResponse} (null if they've never been admitted), and that
 * stay's daily analyses oldest-first (empty when there is no hospitalisation record).
 */
public record PatientClinicalSummaryResponse(
    PatientResponse patient,
    boolean isHospitalised,
    HospitalisationRecordResponse currentHospitalisation,
    List<DailyAnalysisResponse> dailyAnalyses) {
}
