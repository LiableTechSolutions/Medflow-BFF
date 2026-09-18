package com.medflow.modules.patients.domain.repository;

import com.medflow.modules.patients.domain.entity.DailyAnalysis;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyAnalysisRepository extends JpaRepository<DailyAnalysis, Long> {

  List<DailyAnalysis> findByHospitalisationRecordIdOrderByRecordedAtAsc(Long hospitalisationRecordId);

  List<DailyAnalysis> findByPatientIdOrderByRecordedAtAsc(Long patientId);
}
