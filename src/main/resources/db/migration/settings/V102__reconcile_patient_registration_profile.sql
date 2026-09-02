-- Reconcile databases where V101 was previously applied with the legacy profile shape.
-- All statements are idempotent so new databases remain compatible as well.
CREATE TABLE IF NOT EXISTS hospital_registration_profiles (
    hospital_id       BIGINT PRIMARY KEY,
    template          VARCHAR(30) NOT NULL,
    field_states      JSONB NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by        BIGINT,
    CONSTRAINT fk_registration_profile_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_registration_profile_user FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT ck_registration_profile_template CHECK (template IN ('BASIC', 'COMPREHENSIVE'))
);

ALTER TABLE patients ADD COLUMN IF NOT EXISTS middle_name VARCHAR(100);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS biological_sex VARCHAR(20);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS marital_status VARCHAR(30);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(80);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS alternate_number VARCHAR(20);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS address_line_1 VARCHAR(500);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS address_line_2 VARCHAR(500);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS state VARCHAR(100);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS postal_code VARCHAR(30);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS country VARCHAR(100);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS emergency_contact_relation VARCHAR(80);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS insurance_provider VARCHAR(200);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS policy_number VARCHAR(100);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS group_number VARCHAR(100);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS primary_cardholder_name VARCHAR(200);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS primary_physician_id BIGINT;
ALTER TABLE patients ADD COLUMN IF NOT EXISTS referring_doctor VARCHAR(200);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS allergies_summary VARCHAR(2000);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS current_medications VARCHAR(4000);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS clinical_notes VARCHAR(4000);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS registration_data JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE INDEX IF NOT EXISTS idx_registration_profiles_updated
    ON hospital_registration_profiles (updated_at);
