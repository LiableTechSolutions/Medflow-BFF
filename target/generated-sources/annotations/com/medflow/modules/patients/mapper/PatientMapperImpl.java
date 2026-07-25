package com.medflow.modules.patients.mapper;

import com.medflow.modules.patients.api.PatientStatus;
import com.medflow.modules.patients.api.PatientSummary;
import com.medflow.modules.patients.api.response.PatientResponse;
import com.medflow.modules.patients.domain.entity.Patient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T20:09:14+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class PatientMapperImpl implements PatientMapper {

    @Override
    public PatientResponse toResponse(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        UUID id = null;
        String firstName = null;
        String lastName = null;
        LocalDate dateOfBirth = null;
        String gender = null;
        String email = null;
        String phone = null;
        String bloodGroup = null;
        String address = null;
        PatientStatus status = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = patient.getId();
        firstName = patient.getFirstName();
        lastName = patient.getLastName();
        dateOfBirth = patient.getDateOfBirth();
        gender = patient.getGender();
        email = patient.getEmail();
        phone = patient.getPhone();
        bloodGroup = patient.getBloodGroup();
        address = patient.getAddress();
        status = patient.getStatus();
        createdAt = patient.getCreatedAt();
        updatedAt = patient.getUpdatedAt();

        PatientResponse patientResponse = new PatientResponse( id, firstName, lastName, dateOfBirth, gender, email, phone, bloodGroup, address, status, createdAt, updatedAt );

        return patientResponse;
    }

    @Override
    public PatientSummary toSummary(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        UUID id = null;
        String fullName = null;

        id = patient.getId();
        fullName = patient.getFullName();

        PatientSummary patientSummary = new PatientSummary( id, fullName );

        return patientSummary;
    }
}
