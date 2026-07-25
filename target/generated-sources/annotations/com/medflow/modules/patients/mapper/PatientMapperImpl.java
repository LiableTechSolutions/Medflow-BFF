package com.medflow.modules.patients.mapper;

import com.medflow.modules.patients.api.response.PatientResponse;
import com.medflow.modules.patients.domain.entity.Patient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T17:41:01+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12 (Oracle Corporation)"
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
        Instant createdAt = null;

        id = patient.getId();
        firstName = patient.getFirstName();
        lastName = patient.getLastName();
        dateOfBirth = patient.getDateOfBirth();
        gender = patient.getGender();
        email = patient.getEmail();
        createdAt = patient.getCreatedAt();

        PatientResponse patientResponse = new PatientResponse( id, firstName, lastName, dateOfBirth, gender, email, createdAt );

        return patientResponse;
    }
}
