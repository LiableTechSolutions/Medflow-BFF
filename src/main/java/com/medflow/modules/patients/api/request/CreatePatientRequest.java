package com.medflow.modules.patients.api.request;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record CreatePatientRequest(@NotBlank @Size(max = 100) String firstName, @NotBlank @Size(max = 100) String lastName, @NotNull @Past LocalDate dateOfBirth, @NotBlank @Pattern(regexp = "MALE|FEMALE|OTHER") String gender, @Email @Size(max = 255) String email) { }
