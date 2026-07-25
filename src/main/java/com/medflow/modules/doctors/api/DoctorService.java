package com.medflow.modules.doctors.api;

import com.medflow.modules.doctors.api.request.CreateDoctorRequest;
import com.medflow.modules.doctors.api.request.UpdateDoctorRequest;
import com.medflow.modules.doctors.api.response.DoctorResponse;
import com.medflow.shared.api.PageResponse;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Public API of the Doctors module. */
public interface DoctorService {

  DoctorResponse create(CreateDoctorRequest request);

  DoctorResponse findById(UUID doctorId);

  DoctorResponse update(UUID doctorId, UpdateDoctorRequest request);

  DoctorResponse changeAvailability(UUID doctorId, DoctorAvailability availability);

  PageResponse<DoctorResponse> search(String query, String specialty,
      DoctorAvailability availability, int page, int size);

  List<DoctorSummary> summariesByIds(Collection<UUID> doctorIds);

  long count();
}
