package com.medflow.modules.prescriptions.domain.repository;

import com.medflow.modules.prescriptions.domain.entity.Prescription;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrescriptionRepository
    extends JpaRepository<Prescription, UUID>, JpaSpecificationExecutor<Prescription> {
}
