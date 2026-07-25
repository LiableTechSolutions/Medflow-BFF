package com.medflow.modules.laboratory.domain.repository;

import com.medflow.modules.laboratory.api.LabOrderStatus;
import com.medflow.modules.laboratory.domain.entity.LabOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LabOrderRepository
    extends JpaRepository<LabOrder, UUID>, JpaSpecificationExecutor<LabOrder> {

  long countByStatus(LabOrderStatus status);
}
