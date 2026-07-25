package com.medflow.modules.patients.api;
import com.medflow.modules.patients.api.request.CreatePatientRequest;
import com.medflow.modules.patients.api.response.PatientResponse;
import java.util.UUID;
public interface PatientService { PatientResponse create(CreatePatientRequest request); PatientResponse findById(UUID patientId); }
