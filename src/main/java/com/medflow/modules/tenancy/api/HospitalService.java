package com.medflow.modules.tenancy.api;

import com.medflow.modules.tenancy.api.request.RegisterHospitalRequest;
import com.medflow.modules.tenancy.api.request.UpdateHospitalRequest;
import com.medflow.modules.tenancy.api.response.HospitalResponse;

/** Public API of the Tenancy module. */
public interface HospitalService {

  /**
   * Onboards a tenant and entitles it to every generally available module. Called when
   * the first user of a workspace signs up.
   */
  HospitalResponse register(RegisterHospitalRequest request);

  HospitalResponse findById(Long hospitalId);

  HospitalResponse update(Long hospitalId, UpdateHospitalRequest request);

  HospitalSummary summary(Long hospitalId);
}
