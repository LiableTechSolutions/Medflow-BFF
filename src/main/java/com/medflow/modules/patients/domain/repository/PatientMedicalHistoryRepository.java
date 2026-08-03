package com.medflow.modules.patients.domain.repository;

import com.medflow.modules.patients.domain.entity.PatientMedicalHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientMedicalHistoryRepository extends JpaRepository<PatientMedicalHistory, Long> {

  List<PatientMedicalHistory> findByPatientIdOrderByRecordedAtDesc(Long patientId);
}
