package com.medflow.modules.tenancy.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterHospitalRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 200) String legalName,
    @Size(max = 50) String hospitalType,
    @Size(max = 200) String addressLine1,
    @Size(max = 100) String city,
    @Size(max = 100) String state,
    @Size(max = 100) String country,
    @Size(max = 10) String pincode,
    @Size(max = 20) String phone,
    @Email @Size(max = 120) String email,
    @Size(max = 50) String timezone) {
}
