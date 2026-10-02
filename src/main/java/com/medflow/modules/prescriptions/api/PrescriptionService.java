package com.medflow.modules.prescriptions.api;

import com.medflow.modules.prescriptions.api.request.CreatePrescriptionRequest;
import com.medflow.modules.prescriptions.api.response.PrescriptionResponse;
import com.medflow.shared.api.PageResponse;
import java.time.LocalDate;

/** Public API of the Prescriptions module. */
public interface PrescriptionService {

  PrescriptionResponse create(Long hospitalId, CreatePrescriptionRequest request);

  PrescriptionResponse findById(Long hospitalId, Long prescriptionId);

  PageResponse<PrescriptionResponse> search(Long hospitalId, Long patientId, Long doctorId,
      PrescriptionStatus status, LocalDate issuedOn, String query, int page, int size);

  PrescriptionResponse complete(Long hospitalId, Long prescriptionId);

  PrescriptionResponse cancel(Long hospitalId, Long prescriptionId);

  /** Staff explicitly asked to send this prescription to the patient (email/WhatsApp/SMS). */
  void send(Long hospitalId, Long prescriptionId);

  long countByHospital(Long hospitalId);
}
