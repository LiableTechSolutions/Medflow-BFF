package com.medflow.modules.patients.api.response;
import java.time.*; import java.util.UUID;
public record PatientResponse(UUID id, String firstName, String lastName, LocalDate dateOfBirth, String gender, String email, Instant createdAt) { }
