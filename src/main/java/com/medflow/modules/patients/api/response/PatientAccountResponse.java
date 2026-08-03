package com.medflow.modules.patients.api.response;

import com.medflow.modules.patients.api.MappingRelation;
import java.time.Instant;

public record PatientAccountResponse(
    Long id,
    Long userId,
    String userFullName,
    String userEmail,
    MappingRelation relation,
    boolean primaryContact,
    Instant createdAt) {
}
