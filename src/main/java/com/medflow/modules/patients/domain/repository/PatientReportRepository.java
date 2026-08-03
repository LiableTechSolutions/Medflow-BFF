package com.medflow.modules.patients.domain.repository;

import com.medflow.modules.patients.domain.entity.PatientReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientReportRepository extends JpaRepository<PatientReport, Long> {

  List<PatientReport> findByPatientIdOrderByUploadedAtDesc(Long patientId);

  long countByHospitalId(Long hospitalId);
}
