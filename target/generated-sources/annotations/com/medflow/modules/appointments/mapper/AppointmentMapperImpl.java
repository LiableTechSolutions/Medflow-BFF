package com.medflow.modules.appointments.mapper;

import com.medflow.modules.appointments.api.AppointmentStatus;
import com.medflow.modules.appointments.api.response.AppointmentResponse;
import com.medflow.modules.appointments.domain.entity.Appointment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T20:09:13+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class AppointmentMapperImpl implements AppointmentMapper {

    @Override
    public AppointmentResponse toResponse(Appointment appointment, String patientName, String doctorName) {
        if ( appointment == null && patientName == null && doctorName == null ) {
            return null;
        }

        UUID id = null;
        UUID patientId = null;
        UUID doctorId = null;
        Instant scheduledAt = null;
        int durationMinutes = 0;
        String reason = null;
        AppointmentStatus status = null;
        BigDecimal consultationFee = null;
        String notes = null;
        Instant createdAt = null;
        Instant updatedAt = null;
        if ( appointment != null ) {
            id = appointment.getId();
            patientId = appointment.getPatientId();
            doctorId = appointment.getDoctorId();
            scheduledAt = appointment.getScheduledAt();
            durationMinutes = appointment.getDurationMinutes();
            reason = appointment.getReason();
            status = appointment.getStatus();
            consultationFee = appointment.getConsultationFee();
            notes = appointment.getNotes();
            createdAt = appointment.getCreatedAt();
            updatedAt = appointment.getUpdatedAt();
        }
        String patientName1 = null;
        patientName1 = patientName;
        String doctorName1 = null;
        doctorName1 = doctorName;

        AppointmentResponse appointmentResponse = new AppointmentResponse( id, patientId, patientName1, doctorId, doctorName1, scheduledAt, durationMinutes, reason, status, consultationFee, notes, createdAt, updatedAt );

        return appointmentResponse;
    }
}
