package com.medflow.modules.doctors.api;

import com.medflow.modules.doctors.api.request.AddAvailabilityRequest;
import com.medflow.modules.doctors.api.request.AssignStaffRequest;
import com.medflow.modules.doctors.api.request.CreateDoctorRequest;
import com.medflow.modules.doctors.api.request.UpdateDoctorRequest;
import com.medflow.modules.doctors.api.response.DoctorAvailabilityResponse;
import com.medflow.modules.doctors.api.response.DoctorResponse;
import com.medflow.modules.doctors.api.response.DoctorStaffResponse;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.domain.AccountStatus;
import java.util.Collection;
import java.util.List;

/** Public API of the Doctors module. */
public interface DoctorService {

  /** Onboards a doctor: creates the user account (role DOCTOR) and the clinical profile. */
  DoctorResponse create(Long hospitalId, CreateDoctorRequest request);

  DoctorResponse findById(Long hospitalId, Long doctorId);

  DoctorResponse update(Long hospitalId, Long doctorId, UpdateDoctorRequest request);

  DoctorResponse changeStatus(Long hospitalId, Long doctorId, AccountStatus status);

  PageResponse<DoctorResponse> search(Long hospitalId, String query, String specialty,
      AccountStatus status, int page, int size);

  List<DoctorAvailabilityResponse> availability(Long hospitalId, Long doctorId);

  DoctorAvailabilityResponse addAvailability(Long hospitalId, Long doctorId,
      AddAvailabilityRequest request);

  void removeAvailability(Long hospitalId, Long doctorId, Long availabilityId);

  List<DoctorStaffResponse> staff(Long hospitalId, Long doctorId);

  DoctorStaffResponse assignStaff(Long hospitalId, Long doctorId, AssignStaffRequest request);

  /** Batch lookup used by appointments, prescriptions and laboratory. */
  List<DoctorSummary> summariesByIds(Long hospitalId, Collection<Long> doctorIds);

  long countByHospital(Long hospitalId);
}
