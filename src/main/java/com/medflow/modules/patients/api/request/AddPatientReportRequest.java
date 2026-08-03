package com.medflow.modules.patients.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registers a report against a patient. The file itself lives in object storage; the
 * database keeps the pointer, which is what the reference design stores too.
 */
public record AddPatientReportRequest(
    @NotBlank @Size(max = 100) String reportType,
    @NotBlank @Size(max = 255) String fileUrl) {
}
