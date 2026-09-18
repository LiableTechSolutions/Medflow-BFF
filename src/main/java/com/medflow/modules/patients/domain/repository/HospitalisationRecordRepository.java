package com.medflow.modules.patients.domain.repository;

import com.medflow.modules.patients.api.HospitalisationStatus;
import com.medflow.modules.patients.domain.entity.HospitalisationRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalisationRecordRepository extends JpaRepository<HospitalisationRecord, Long> {

  Optional<HospitalisationRecord> findFirstByPatientIdAndStatusOrderByAdmissionDateDesc(
      Long patientId, HospitalisationStatus status);

  Optional<HospitalisationRecord> findFirstByPatientIdOrderByAdmissionDateDesc(Long patientId);

  List<HospitalisationRecord> findByPatientIdOrderByAdmissionDateDesc(Long patientId);
}
