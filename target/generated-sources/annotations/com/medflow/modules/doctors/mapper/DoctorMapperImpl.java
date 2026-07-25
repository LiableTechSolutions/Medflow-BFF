package com.medflow.modules.doctors.mapper;

import com.medflow.modules.doctors.api.DoctorAvailability;
import com.medflow.modules.doctors.api.DoctorSummary;
import com.medflow.modules.doctors.api.response.DoctorResponse;
import com.medflow.modules.doctors.domain.entity.Doctor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T19:48:23+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class DoctorMapperImpl implements DoctorMapper {

    @Override
    public DoctorResponse toResponse(Doctor doctor) {
        if ( doctor == null ) {
            return null;
        }

        UUID id = null;
        String fullName = null;
        String email = null;
        String phone = null;
        String specialty = null;
        String department = null;
        String licenseNumber = null;
        DoctorAvailability availability = null;
        BigDecimal consultationFee = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = doctor.getId();
        fullName = doctor.getFullName();
        email = doctor.getEmail();
        phone = doctor.getPhone();
        specialty = doctor.getSpecialty();
        department = doctor.getDepartment();
        licenseNumber = doctor.getLicenseNumber();
        availability = doctor.getAvailability();
        consultationFee = doctor.getConsultationFee();
        createdAt = doctor.getCreatedAt();
        updatedAt = doctor.getUpdatedAt();

        DoctorResponse doctorResponse = new DoctorResponse( id, fullName, email, phone, specialty, department, licenseNumber, availability, consultationFee, createdAt, updatedAt );

        return doctorResponse;
    }

    @Override
    public DoctorSummary toSummary(Doctor doctor) {
        if ( doctor == null ) {
            return null;
        }

        UUID id = null;
        String fullName = null;
        String specialty = null;
        BigDecimal consultationFee = null;

        id = doctor.getId();
        fullName = doctor.getFullName();
        specialty = doctor.getSpecialty();
        consultationFee = doctor.getConsultationFee();

        DoctorSummary doctorSummary = new DoctorSummary( id, fullName, specialty, consultationFee );

        return doctorSummary;
    }
}
