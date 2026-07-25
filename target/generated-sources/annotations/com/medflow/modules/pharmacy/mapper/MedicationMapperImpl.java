package com.medflow.modules.pharmacy.mapper;

import com.medflow.modules.pharmacy.api.response.MedicationResponse;
import com.medflow.modules.pharmacy.domain.entity.Medication;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T20:09:13+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class MedicationMapperImpl implements MedicationMapper {

    @Override
    public MedicationResponse toResponse(Medication medication) {
        if ( medication == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        String category = null;
        BigDecimal unitPrice = null;
        int stockQuantity = 0;
        int reorderLevel = 0;
        boolean lowStock = false;
        LocalDate expiryDate = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = medication.getId();
        name = medication.getName();
        category = medication.getCategory();
        unitPrice = medication.getUnitPrice();
        stockQuantity = medication.getStockQuantity();
        reorderLevel = medication.getReorderLevel();
        lowStock = medication.isLowStock();
        expiryDate = medication.getExpiryDate();
        createdAt = medication.getCreatedAt();
        updatedAt = medication.getUpdatedAt();

        MedicationResponse medicationResponse = new MedicationResponse( id, name, category, unitPrice, stockQuantity, reorderLevel, lowStock, expiryDate, createdAt, updatedAt );

        return medicationResponse;
    }
}
