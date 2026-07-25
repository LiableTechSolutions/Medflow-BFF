package com.medflow.modules.laboratory.mapper;

import com.medflow.modules.laboratory.api.LabOrderStatus;
import com.medflow.modules.laboratory.api.LabPriority;
import com.medflow.modules.laboratory.api.response.LabOrderResponse;
import com.medflow.modules.laboratory.domain.entity.LabOrder;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T19:48:22+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class LabOrderMapperImpl implements LabOrderMapper {

    @Override
    public LabOrderResponse toResponse(LabOrder labOrder, String patientName, String orderedByName) {
        if ( labOrder == null && patientName == null && orderedByName == null ) {
            return null;
        }

        UUID id = null;
        UUID patientId = null;
        UUID orderedBy = null;
        String testName = null;
        LabPriority priority = null;
        LabOrderStatus status = null;
        String resultSummary = null;
        Instant orderedAt = null;
        Instant completedAt = null;
        Instant updatedAt = null;
        if ( labOrder != null ) {
            id = labOrder.getId();
            patientId = labOrder.getPatientId();
            orderedBy = labOrder.getOrderedBy();
            testName = labOrder.getTestName();
            priority = labOrder.getPriority();
            status = labOrder.getStatus();
            resultSummary = labOrder.getResultSummary();
            orderedAt = labOrder.getOrderedAt();
            completedAt = labOrder.getCompletedAt();
            updatedAt = labOrder.getUpdatedAt();
        }
        String patientName1 = null;
        patientName1 = patientName;
        String orderedByName1 = null;
        orderedByName1 = orderedByName;

        LabOrderResponse labOrderResponse = new LabOrderResponse( id, patientId, patientName1, orderedBy, orderedByName1, testName, priority, status, resultSummary, orderedAt, completedAt, updatedAt );

        return labOrderResponse;
    }
}
