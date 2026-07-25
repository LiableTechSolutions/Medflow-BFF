package com.medflow.modules.prescriptions.api;

import com.medflow.modules.prescriptions.api.request.CreatePrescriptionRequest;
import com.medflow.modules.prescriptions.api.response.PrescriptionResponse;
import com.medflow.shared.api.PageResponse;
import java.util.UUID;

/** Public API of the Prescriptions module. */
public interface PrescriptionService {

  PrescriptionResponse create(CreatePrescriptionRequest request);

  PrescriptionResponse findById(UUID prescriptionId);

  PageResponse<PrescriptionResponse> search(UUID patientId, UUID doctorId,
      PrescriptionStatus status, int page, int size);

  PrescriptionResponse complete(UUID prescriptionId);

  PrescriptionResponse cancel(UUID prescriptionId);
}
