-- Patient Registration Profile Configuration
-- Stores hospital-specific configuration for patient registration fields

CREATE TABLE patient_registration_profiles (
    id BIGSERIAL PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    profile_name VARCHAR(100) NOT NULL DEFAULT 'Default',
    active BOOLEAN NOT NULL DEFAULT true,
    version INT NOT NULL DEFAULT 1,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(hospital_id, active)
);

CREATE TABLE patient_registration_profile_fields (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES patient_registration_profiles(id) ON DELETE CASCADE,
    field_key VARCHAR(50) NOT NULL,
    field_label VARCHAR(100) NOT NULL,
    field_group VARCHAR(50) NOT NULL,
    field_state VARCHAR(20) NOT NULL CHECK (field_state IN ('REQUIRED', 'OPTIONAL', 'HIDDEN')),
    field_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(profile_id, field_key)
);

CREATE INDEX idx_patient_registration_profiles_hospital_id ON patient_registration_profiles(hospital_id);
CREATE INDEX idx_patient_registration_profiles_hospital_active ON patient_registration_profiles(hospital_id, active);
CREATE INDEX idx_patient_registration_profile_fields_profile_id ON patient_registration_profile_fields(profile_id);
CREATE INDEX idx_patient_registration_profile_fields_field_key ON patient_registration_profile_fields(field_key);
