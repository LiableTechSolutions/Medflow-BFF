package com.medflow.modules.prescriptions.mapper;

import com.medflow.modules.prescriptions.api.PrescriptionStatus;
import com.medflow.modules.prescriptions.api.response.PrescriptionItemResponse;
import com.medflow.modules.prescriptions.api.response.PrescriptionResponse;
import com.medflow.modules.prescriptions.domain.entity.Prescription;
import com.medflow.modules.prescriptions.domain.entity.PrescriptionItem;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T20:09:14+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class PrescriptionMapperImpl implements PrescriptionMapper {

    @Override
    public PrescriptionResponse toResponse(Prescription prescription, String patientName, String doctorName) {
        if ( prescription == null && patientName == null && doctorName == null ) {
            return null;
        }

        UUID id = null;
        UUID patientId = null;
        UUID doctorId = null;
        PrescriptionStatus status = null;
        String notes = null;
        List<PrescriptionItemResponse> items = null;
        Instant issuedAt = null;
        Instant updatedAt = null;
        if ( prescription != null ) {
            id = prescription.getId();
            patientId = prescription.getPatientId();
            doctorId = prescription.getDoctorId();
            status = prescription.getStatus();
            notes = prescription.getNotes();
            items = prescriptionItemListToPrescriptionItemResponseList( prescription.getItems() );
            issuedAt = prescription.getIssuedAt();
            updatedAt = prescription.getUpdatedAt();
        }
        String patientName1 = null;
        patientName1 = patientName;
        String doctorName1 = null;
        doctorName1 = doctorName;

        PrescriptionResponse prescriptionResponse = new PrescriptionResponse( id, patientId, patientName1, doctorId, doctorName1, status, notes, items, issuedAt, updatedAt );

        return prescriptionResponse;
    }

    @Override
    public PrescriptionItemResponse toItemResponse(PrescriptionItem item) {
        if ( item == null ) {
            return null;
        }

        UUID id = null;
        String medicationName = null;
        String dosage = null;
        String frequency = null;
        Integer durationDays = null;
        String instructions = null;

        id = item.getId();
        medicationName = item.getMedicationName();
        dosage = item.getDosage();
        frequency = item.getFrequency();
        durationDays = item.getDurationDays();
        instructions = item.getInstructions();

        PrescriptionItemResponse prescriptionItemResponse = new PrescriptionItemResponse( id, medicationName, dosage, frequency, durationDays, instructions );

        return prescriptionItemResponse;
    }

    protected List<PrescriptionItemResponse> prescriptionItemListToPrescriptionItemResponseList(List<PrescriptionItem> list) {
        if ( list == null ) {
            return null;
        }

        List<PrescriptionItemResponse> list1 = new ArrayList<PrescriptionItemResponse>( list.size() );
        for ( PrescriptionItem prescriptionItem : list ) {
            list1.add( toItemResponse( prescriptionItem ) );
        }

        return list1;
    }
}
