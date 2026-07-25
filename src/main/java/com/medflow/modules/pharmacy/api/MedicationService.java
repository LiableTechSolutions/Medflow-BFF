package com.medflow.modules.pharmacy.api;

import com.medflow.modules.pharmacy.api.request.AdjustStockRequest;
import com.medflow.modules.pharmacy.api.request.CreateMedicationRequest;
import com.medflow.modules.pharmacy.api.request.UpdateMedicationRequest;
import com.medflow.modules.pharmacy.api.response.MedicationResponse;
import com.medflow.shared.api.PageResponse;
import java.util.UUID;

/** Public API of the Pharmacy module. */
public interface MedicationService {

  MedicationResponse create(CreateMedicationRequest request);

  MedicationResponse findById(UUID medicationId);

  MedicationResponse update(UUID medicationId, UpdateMedicationRequest request);

  PageResponse<MedicationResponse> search(String query, boolean lowStockOnly, int page, int size);

  /** Positive delta restocks, negative delta dispenses; stock can never go below zero. */
  MedicationResponse adjustStock(UUID medicationId, AdjustStockRequest request);
}
