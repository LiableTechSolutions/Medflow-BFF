-- =====================================================================================
-- Extra test data on top of the demo workspace Flyway already seeded.
--
-- Run connected to the "medflow" database (pgAdmin → Query Tool → F5). Safe to run more
-- than once: every run appends a fresh batch with correctly numbered codes.
--
-- PostgreSQL-specific on purpose — this is a manual convenience script, never a
-- migration. Prefer the REST API when you want realistic data (it generates codes,
-- hashes passwords and assigns queue numbers for you); see PGADMIN_SETUP.md.
--
-- Two rules if you write your own inserts:
--   * every row belongs to a hospital — the demo one is id 1;
--   * enum-typed columns use the design's lower-case values ('active', 'walk_in',
--     'female', 'booked', …). CHECK constraints reject anything else.
-- =====================================================================================

-- --------------------------------------------------------------------- patients (+6)
WITH counter AS (
    SELECT count(*) AS taken FROM patients WHERE hospital_id = 1
), incoming AS (
    SELECT * FROM (VALUES
        ('Aarav',  'Menon',   'male',   DATE '1988-02-14', 'B+',  '+91 90000 10001', 'aarav.menon@example.com',   '4 Frazer Town, Bengaluru'),
        ('Diya',   'Sharma',  'female', DATE '1996-08-09', 'A+',  '+91 90000 10002', 'diya.sharma@example.com',   '18 HSR Layout, Bengaluru'),
        ('Kabir',  'Singh',   'male',   DATE '1972-11-30', 'O+',  '+91 90000 10003', 'kabir.singh@example.com',   '90 Basavanagudi, Bengaluru'),
        ('Anika',  'Fernandes','female',DATE '2011-05-22', 'AB-', '+91 90000 10004', 'anika.f@example.com',       '3 Cooke Town, Bengaluru'),
        ('Rahul',  'Bose',    'male',   DATE '1983-01-05', 'B-',  '+91 90000 10005', 'rahul.bose@example.com',    '77 Ulsoor, Bengaluru'),
        ('Sneha',  'Pillai',  'female', DATE '1990-07-17', 'O-',  '+91 90000 10006', 'sneha.pillai@example.com',  '22 Rajajinagar, Bengaluru')
    ) AS t(first_name, last_name, gender, date_of_birth, blood_group, phone, email, address)
)
INSERT INTO patients (hospital_id, patient_code, first_name, last_name, gender, date_of_birth,
                      blood_group, phone, email, address, emergency_contact_name,
                      emergency_contact_phone, status, created_at, updated_at, is_deleted)
SELECT 1,
       'PAT-' || LPAD((counter.taken + ROW_NUMBER() OVER (ORDER BY incoming.first_name))::text, 6, '0'),
       incoming.first_name, incoming.last_name, incoming.gender, incoming.date_of_birth,
       incoming.blood_group, incoming.phone,
       -- keep emails unique across re-runs (the table has a per-hospital unique index)
       counter.taken || '.' || incoming.email,
       incoming.address, 'Emergency contact', '+91 90000 99999',
       'active', now(), now(), FALSE
FROM incoming, counter;

-- ----------------------------------------------------------------- appointments (+18)
-- The two newest patients booked with every doctor across the next three days, so the
-- dashboard, the calendar and the appointment queue all have something to show.
INSERT INTO appointments (hospital_id, doctor_id, patient_id, appointment_mode, scheduled_at,
                          duration_minutes, status, queue_number, booked_by_user_id, reason,
                          consultation_fee, created_at, updated_at)
SELECT 1,
       d.id,
       p.id,
       CASE WHEN (slot.n % 2) = 0 THEN 'online' ELSE 'walk_in' END,
       CURRENT_DATE + (slot.n || ' day')::interval + INTERVAL '10' HOUR + (d.id || ' hour')::interval,
       30,
       CASE slot.n WHEN 0 THEN 'confirmed' WHEN 1 THEN 'booked' ELSE 'booked' END,
       ROW_NUMBER() OVER (PARTITION BY d.id, slot.n ORDER BY p.id),
       (SELECT id FROM users WHERE hospital_id = 1 AND email = 'reception@medflow.local'),
       'Sample visit',
       d.consultation_fee,
       now(), now()
FROM doctors d
CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2) slot
JOIN LATERAL (
    SELECT id FROM patients
    WHERE hospital_id = 1 AND is_deleted = FALSE
    ORDER BY id DESC LIMIT 2
) p ON TRUE
WHERE d.hospital_id = 1;

-- ------------------------------------------------------------------ medical history (+)
INSERT INTO patient_medical_history (patient_id, condition_name, notes, recorded_by_doctor_id, recorded_at)
SELECT p.id, 'General check-up', 'Baseline observations recorded at registration.',
       (SELECT id FROM doctors WHERE hospital_id = 1 ORDER BY id LIMIT 1), now()
FROM patients p
WHERE p.hospital_id = 1
  AND NOT EXISTS (SELECT 1 FROM patient_medical_history h WHERE h.patient_id = p.id);

-- ------------------------------------------------------------------------ pharmacy (+)
INSERT INTO medications (hospital_id, name, category, unit_price, stock_quantity, reorder_level,
                         expiry_date, created_at, updated_at)
SELECT 1, name, category, unit_price, stock_quantity, reorder_level,
       CURRENT_DATE + INTERVAL '365' DAY, now(), now()
FROM (VALUES
    ('Azithromycin 500mg', 'Antibiotic',      24.00, 140,  50),
    ('Cetirizine 10mg',    'Antihistamine',    1.80, 400, 120),
    ('Pantoprazole 40mg',  'Gastro',           5.60,  35,  60),
    ('Vitamin D3 60K',     'Supplement',      32.00,  90,  40)
) AS m(name, category, unit_price, stock_quantity, reorder_level)
WHERE NOT EXISTS (
    SELECT 1 FROM medications x WHERE x.hospital_id = 1 AND x.name = m.name
);

-- ---------------------------------------------------------------------- lab orders (+)
INSERT INTO lab_orders (hospital_id, patient_id, doctor_id, test_name, priority, status,
                        ordered_at, updated_at)
SELECT 1, p.id,
       (SELECT id FROM doctors WHERE hospital_id = 1 ORDER BY id LIMIT 1),
       'Thyroid profile', 'ROUTINE', 'ORDERED', now(), now()
FROM patients p
WHERE p.hospital_id = 1
ORDER BY p.id DESC
LIMIT 3;

-- What you just created:
SELECT 'patients' AS table_name, count(*) FROM patients WHERE hospital_id = 1
UNION ALL SELECT 'appointments', count(*) FROM appointments WHERE hospital_id = 1
UNION ALL SELECT 'medications',  count(*) FROM medications  WHERE hospital_id = 1
UNION ALL SELECT 'lab_orders',   count(*) FROM lab_orders   WHERE hospital_id = 1
ORDER BY 1;
