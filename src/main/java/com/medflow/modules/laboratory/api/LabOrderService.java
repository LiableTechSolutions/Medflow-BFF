package com.medflow.modules.laboratory.api;

import com.medflow.modules.laboratory.api.request.CompleteLabOrderRequest;
import com.medflow.modules.laboratory.api.request.CreateLabOrderRequest;
import com.medflow.modules.laboratory.api.response.LabOrderResponse;
import com.medflow.shared.api.PageResponse;
import java.util.UUID;

/** Public API of the Laboratory module. */
public interface LabOrderService {

  LabOrderResponse create(CreateLabOrderRequest request);

  LabOrderResponse findById(UUID labOrderId);

  PageResponse<LabOrderResponse> search(LabOrderStatus status, LabPriority priority,
      UUID patientId, int page, int size);

  LabOrderResponse startProcessing(UUID labOrderId);

  LabOrderResponse complete(UUID labOrderId, CompleteLabOrderRequest request);

  LabOrderResponse cancel(UUID labOrderId);

  /** Used by analytics for the "reports filed" KPI. */
  long countCompleted();
}
