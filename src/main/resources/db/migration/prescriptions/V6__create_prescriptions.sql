CREATE TABLE prescriptions (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(1000),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_prescriptions_patient ON prescriptions (patient_id);
CREATE INDEX idx_prescriptions_doctor ON prescriptions (doctor_id);

CREATE TABLE prescription_items (
    id UUID PRIMARY KEY,
    prescription_id UUID NOT NULL,
    medication_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    duration_days INTEGER,
    instructions VARCHAR(255),
    CONSTRAINT fk_prescription_items_prescription
        FOREIGN KEY (prescription_id) REFERENCES prescriptions (id) ON DELETE CASCADE
);

CREATE INDEX idx_prescription_items_prescription ON prescription_items (prescription_id);
