package com.medflow.modules.beds.api;

import com.medflow.modules.beds.api.request.AssignBedRequest;
import com.medflow.modules.beds.api.request.BedCountRequest;
import com.medflow.modules.beds.api.request.CreateWardRequest;
import com.medflow.modules.beds.api.response.BedResponse;
import com.medflow.modules.beds.api.response.BedSummaryResponse;
import com.medflow.modules.beds.api.response.WardResponse;
import java.util.List;

/** Public API of the Beds module. */
public interface BedService {

  WardResponse createWard(Long hospitalId, CreateWardRequest request);

  List<WardResponse> wards(Long hospitalId);

  List<BedResponse> beds(Long hospitalId, Long wardId);

  /** Grows the ward by {@code count} beds, numbered on from the highest existing one. */
  WardResponse addBeds(Long hospitalId, Long wardId, BedCountRequest request);

  /** Removes {@code count} free beds (highest numbers first); never touches occupied ones. */
  WardResponse reduceBeds(Long hospitalId, Long wardId, BedCountRequest request);

  BedResponse assign(Long hospitalId, Long bedId, AssignBedRequest request);

  BedResponse release(Long hospitalId, Long bedId);

  BedResponse setMaintenance(Long hospitalId, Long bedId, boolean underMaintenance);

  BedSummaryResponse summary(Long hospitalId);
}
