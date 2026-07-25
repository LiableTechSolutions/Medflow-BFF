CREATE TABLE doctors (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    specialty VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL,
    availability VARCHAR(20) NOT NULL,
    consultation_fee NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_doctors_email UNIQUE (email),
    CONSTRAINT uk_doctors_license UNIQUE (license_number)
);

CREATE INDEX idx_doctors_specialty ON doctors (specialty);
