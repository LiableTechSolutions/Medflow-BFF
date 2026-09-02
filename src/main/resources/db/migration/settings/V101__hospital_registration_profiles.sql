-- Tenant-owned patient registration policy. The JSON object is validated by the settings service
-- against the canonical field catalogue; no patient data is stored here.
CREATE TABLE hospital_registration_profiles (
    hospital_id       BIGINT PRIMARY KEY,
    template          VARCHAR(30) NOT NULL,
    field_states      JSONB NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by        BIGINT,
    CONSTRAINT fk_registration_profile_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_registration_profile_user FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT ck_registration_profile_template CHECK (template IN ('BASIC', 'COMPREHENSIVE'))
);

ALTER TABLE patients ADD COLUMN middle_name VARCHAR(100);
ALTER TABLE patients ADD COLUMN biological_sex VARCHAR(20);
ALTER TABLE patients ADD COLUMN marital_status VARCHAR(30);
ALTER TABLE patients ADD COLUMN preferred_language VARCHAR(80);
ALTER TABLE patients ADD COLUMN alternate_number VARCHAR(20);
ALTER TABLE patients ADD COLUMN address_line_1 VARCHAR(500);
ALTER TABLE patients ADD COLUMN address_line_2 VARCHAR(500);
ALTER TABLE patients ADD COLUMN city VARCHAR(100);
ALTER TABLE patients ADD COLUMN state VARCHAR(100);
ALTER TABLE patients ADD COLUMN postal_code VARCHAR(30);
ALTER TABLE patients ADD COLUMN country VARCHAR(100);
ALTER TABLE patients ADD COLUMN emergency_contact_relation VARCHAR(80);
ALTER TABLE patients ADD COLUMN insurance_provider VARCHAR(200);
ALTER TABLE patients ADD COLUMN policy_number VARCHAR(100);
ALTER TABLE patients ADD COLUMN group_number VARCHAR(100);
ALTER TABLE patients ADD COLUMN primary_cardholder_name VARCHAR(200);
ALTER TABLE patients ADD COLUMN primary_physician_id BIGINT;
ALTER TABLE patients ADD COLUMN referring_doctor VARCHAR(200);
ALTER TABLE patients ADD COLUMN allergies_summary VARCHAR(2000);
ALTER TABLE patients ADD COLUMN current_medications VARCHAR(4000);
ALTER TABLE patients ADD COLUMN clinical_notes VARCHAR(4000);
ALTER TABLE patients ADD COLUMN registration_data JSONB NOT NULL DEFAULT '{}'::jsonb;
CREATE INDEX idx_registration_profiles_updated ON hospital_registration_profiles (updated_at);