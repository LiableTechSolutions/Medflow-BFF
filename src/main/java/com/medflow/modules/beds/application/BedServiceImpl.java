package com.medflow.modules.beds.application;

import com.medflow.modules.beds.api.BedService;
import com.medflow.modules.beds.api.BedStatus;
import com.medflow.modules.beds.api.request.AssignBedRequest;
import com.medflow.modules.beds.api.request.BedCountRequest;
import com.medflow.modules.beds.api.request.CreateWardRequest;
import com.medflow.modules.beds.api.response.BedResponse;
import com.medflow.modules.beds.api.response.BedSummaryResponse;
import com.medflow.modules.beds.api.response.WardResponse;
import com.medflow.modules.beds.domain.entity.Bed;
import com.medflow.modules.beds.domain.entity.Ward;
import com.medflow.modules.beds.domain.repository.BedRepository;
import com.medflow.modules.beds.domain.repository.WardRepository;
import com.medflow.modules.patients.api.PatientService;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.shared.exception.BusinessRuleViolationException;
import com.medflow.shared.exception.DuplicateResourceException;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class BedServiceImpl implements BedService {

  private final WardRepository wards;
  private final BedRepository beds;
  private final PatientService patientService;

  BedServiceImpl(WardRepository wards, BedRepository beds, PatientService patientService) {
    this.wards = wards;
    this.beds = beds;
    this.patientService = patientService;
  }

  @Override
  @Transactional
  public WardResponse createWard(Long hospitalId, CreateWardRequest request) {
    var name = request.name().trim();
    if (wards.existsByHospitalIdAndNameIgnoreCase(hospitalId, name)) {
      throw new DuplicateResourceException("A ward with this name already exists");
    }
    var ward = wards.save(new Ward(hospitalId, name, request.wardType()));
    addBedsTo(ward, request.bedCount());
    return toResponse(ward, beds.findByWardIdAndHospitalIdOrderByBedNumberAsc(ward.getId(), hospitalId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<WardResponse> wards(Long hospitalId) {
    var byWard = beds.findByHospitalId(hospitalId).stream()
        .collect(Collectors.groupingBy(Bed::getWardId));
    return wards.findByHospitalIdOrderByNameAsc(hospitalId).stream()
        .map(ward -> toResponse(ward, byWard.getOrDefault(ward.getId(), List.of())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<BedResponse> beds(Long hospitalId, Long wardId) {
    var ward = loadWard(hospitalId, wardId);
    var list = beds.findByWardIdAndHospitalIdOrderByBedNumberAsc(ward.getId(), hospitalId);
    var patientIds = list.stream().map(Bed::getPatientId).filter(Objects::nonNull).toList();
    Map<Long, PatientSummary> patients = patientIds.isEmpty()
        ? Map.of()
        : patientService.summariesByIds(hospitalId, patientIds).stream()
            .collect(Collectors.toMap(PatientSummary::id, p -> p));
    return list.stream().map(bed -> toResponse(bed, ward, patients)).toList();
  }

  @Override
  @Transactional
  public WardResponse addBeds(Long hospitalId, Long wardId, BedCountRequest request) {
    var ward = loadWard(hospitalId, wardId);
    addBedsTo(ward, request.count());
    return toResponse(ward, beds.findByWardIdAndHospitalIdOrderByBedNumberAsc(wardId, hospitalId));
  }

  @Override
  @Transactional
  public WardResponse reduceBeds(Long hospitalId, Long wardId, BedCountRequest request) {
    var ward = loadWard(hospitalId, wardId);
    var free = beds.findByWardIdAndStatusOrderByBedNumberDesc(wardId, BedStatus.AVAILABLE);
    if (request.count() > free.size()) {
      throw new BusinessRuleViolationException("Can't remove " + request.count() + " bed(s): only "
          + free.size() + " are free (occupied and maintenance beds stay)");
    }
    beds.deleteAll(free.subList(0, request.count()));
    beds.flush();
    return toResponse(ward, beds.findByWardIdAndHospitalIdOrderByBedNumberAsc(wardId, hospitalId));
  }

  @Override
  @Transactional
  public BedResponse assign(Long hospitalId, Long bedId, AssignBedRequest request) {
    var bed = loadBed(hospitalId, bedId);
    var patient = patientService.summariesByIds(hospitalId, List.of(request.patientId())).stream()
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.patientId()));
    if (beds.existsByPatientId(patient.id())) {
      throw new BusinessRuleViolationException("This patient already has a bed");
    }
    bed.assign(patient.id());
    return toResponse(bed, loadWard(hospitalId, bed.getWardId()), Map.of(patient.id(), patient));
  }

  @Override
  @Transactional
  public BedResponse release(Long hospitalId, Long bedId) {
    var bed = loadBed(hospitalId, bedId);
    bed.release();
    return toResponse(bed, loadWard(hospitalId, bed.getWardId()), Map.of());
  }

  @Override
  @Transactional
  public BedResponse setMaintenance(Long hospitalId, Long bedId, boolean underMaintenance) {
    var bed = loadBed(hospitalId, bedId);
    bed.setMaintenance(underMaintenance);
    return toResponse(bed, loadWard(hospitalId, bed.getWardId()), Map.of());
  }

  @Override
  @Transactional(readOnly = true)
  public BedSummaryResponse summary(Long hospitalId) {
    var all = beds.findByHospitalId(hospitalId);
    int available = count(all, BedStatus.AVAILABLE);
    int occupied = count(all, BedStatus.OCCUPIED);
    int maintenance = count(all, BedStatus.MAINTENANCE);
    // Beds under maintenance can't take a patient, so they don't count toward capacity.
    int usable = available + occupied;
    int percent = usable == 0 ? 0 : Math.round(occupied * 100f / usable);
    return new BedSummaryResponse(all.size(), available, occupied, maintenance, percent);
  }

  private void addBedsTo(Ward ward, int count) {
    int next = beds.highestBedNumber(ward.getId());
    for (int i = 1; i <= count; i++) {
      beds.save(new Bed(ward.getHospitalId(), ward.getId(), next + i));
    }
    beds.flush();
  }

  private WardResponse toResponse(Ward ward, List<Bed> wardBeds) {
    return new WardResponse(ward.getId(), ward.getName(), ward.getWardType(), wardBeds.size(),
        count(wardBeds, BedStatus.AVAILABLE), count(wardBeds, BedStatus.OCCUPIED),
        count(wardBeds, BedStatus.MAINTENANCE));
  }

  private static int count(List<Bed> list, BedStatus status) {
    return (int) list.stream().filter(bed -> bed.getStatus() == status).count();
  }

  private BedResponse toResponse(Bed bed, Ward ward, Map<Long, PatientSummary> patients) {
    var patient = bed.getPatientId() == null ? null : patients.get(bed.getPatientId());
    return new BedResponse(bed.getId(), bed.getWardId(),
        ward.getName() + "-" + String.format("%02d", bed.getBedNumber()), bed.getStatus(),
        bed.getPatientId(), patient == null ? null : patient.fullName(), bed.getOccupiedAt());
  }

  private Ward loadWard(Long hospitalId, Long wardId) {
    return wards.findByIdAndHospitalId(wardId, hospitalId)
        .orElseThrow(() -> new ResourceNotFoundException("Ward not found: " + wardId));
  }

  private Bed loadBed(Long hospitalId, Long bedId) {
    return beds.findByIdAndHospitalId(bedId, hospitalId)
        .orElseThrow(() -> new ResourceNotFoundException("Bed not found: " + bedId));
  }
}
