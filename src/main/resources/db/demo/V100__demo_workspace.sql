-- =====================================================================================
-- Demo tenant so a fresh local install shows a populated UI immediately.
--
-- This file lives in classpath:db/demo, which is listed in spring.flyway.locations by
-- default. Drop that location (FLYWAY_LOCATIONS=classpath:db/migration) for a clean
-- production schema — the application does not depend on any of these rows.
--
-- Sign in with:  admin@medflow.local / Admin@12345   (Administrator)
--                kabir@medflow.local / Doctor@12345  (Doctor)
-- =====================================================================================

INSERT INTO hospitals (id, hospital_code, name, legal_name, hospital_type, address_line1, city,
                       state, country, pincode, phone, email, timezone, status, onboarded_at,
                       created_at, updated_at, is_deleted)
VALUES (1, 'MEDFLOW-01', 'MedFlow Clinic', 'MedFlow Healthcare Pvt Ltd', 'multi_speciality',
        '12 Residency Road', 'Bengaluru', 'Karnataka', 'India', '560025', '+91 80 4000 1000',
        'hello@medflow.local', 'Asia/Kolkata', 'active', now(), now(), now(), FALSE);

ALTER TABLE hospitals ALTER COLUMN id RESTART WITH 100;

-- Every phase-1 module is licensed for the demo hospital; phase-2 modules stay disabled.
INSERT INTO hospital_module_entitlements (hospital_id, module_id, status, activated_at, created_at)
SELECT 1, m.id, CASE WHEN m.phase = 1 THEN 'active' ELSE 'disabled' END,
       CASE WHEN m.phase = 1 THEN now() ELSE NULL END, now()
FROM modules_master m;

-- --------------------------------------------------------------------------------- users
-- Passwords: admin => Admin@12345, everyone else => Doctor@12345 (BCrypt, cost 10).
INSERT INTO users (id, hospital_id, user_uid, role_id, user_group_id, first_name, last_name,
                   email, phone, password_hash, gender, date_of_birth, status, created_at,
                   updated_at, is_deleted) VALUES
    (1, 1, '11111111-1111-4111-8111-111111111111', 1, 1, 'Ananya', 'Rao',
     'admin@medflow.local', '+91 98450 10001',
     '$2a$10$hawcrcf2cvjQZ1A4waNkEOdgCeabHdKirrenFYzbUZfoTsk65X1r2', 'female', DATE '1985-02-11',
     'active', now(), now(), FALSE),
    (2, 1, '22222222-2222-4222-8222-222222222222', 2, 2, 'Kabir', 'Shah',
     'kabir@medflow.local', '+91 98450 10002',
     '$2a$10$EWm3UbzbzS/gKxJOW1bhp.LmAg1OeX8BfHORJAW06YWlylNAPt4Lu', 'male', DATE '1980-07-19',
     'active', now(), now(), FALSE),
    (3, 1, '33333333-3333-4333-8333-333333333333', 2, 2, 'Sana', 'Iyer',
     'sana@medflow.local', '+91 98450 10003',
     '$2a$10$EWm3UbzbzS/gKxJOW1bhp.LmAg1OeX8BfHORJAW06YWlylNAPt4Lu', 'female', DATE '1983-11-02',
     'active', now(), now(), FALSE),
    (4, 1, '44444444-4444-4444-8444-444444444444', 2, 2, 'Arjun', 'Mehta',
     'arjun@medflow.local', '+91 98450 10004',
     '$2a$10$EWm3UbzbzS/gKxJOW1bhp.LmAg1OeX8BfHORJAW06YWlylNAPt4Lu', 'male', DATE '1978-05-23',
     'active', now(), now(), FALSE),
    (5, 1, '55555555-5555-4555-8555-555555555555', 6, 1, 'Nikita', 'Sharma',
     'reception@medflow.local', '+91 98450 10005',
     '$2a$10$EWm3UbzbzS/gKxJOW1bhp.LmAg1OeX8BfHORJAW06YWlylNAPt4Lu', 'female', DATE '1994-09-30',
     'active', now(), now(), FALSE),
    (6, 1, '66666666-6666-4666-8666-666666666666', 4, 1, 'Vikram', 'Nair',
     'lab@medflow.local', '+91 98450 10006',
     '$2a$10$EWm3UbzbzS/gKxJOW1bhp.LmAg1OeX8BfHORJAW06YWlylNAPt4Lu', 'male', DATE '1990-01-15',
     'active', now(), now(), FALSE);

ALTER TABLE users ALTER COLUMN id RESTART WITH 100;

-- ------------------------------------------------------------------------------- doctors
INSERT INTO doctors (id, user_id, hospital_id, doctor_code, specialty, qualification,
                     registration_number, years_of_experience, consultation_fee, bio, status,
                     created_at, updated_at) VALUES
    (1, 2, 1, 'DOC-0001', 'Cardiology',  'MBBS, MD (Cardiology)', 'KMC-114455', 14, 1500.00,
     'Interventional cardiologist focused on preventive care.', 'active', now(), now()),
    (2, 3, 1, 'DOC-0002', 'Paediatrics', 'MBBS, DCH',             'KMC-224466', 11, 900.00,
     'Paediatrician with a special interest in neonatal care.',  'active', now(), now()),
    (3, 4, 1, 'DOC-0003', 'Orthopaedics','MBBS, MS (Ortho)',      'KMC-334477', 18, 1200.00,
     'Joint replacement and sports injury specialist.',          'active', now(), now());

ALTER TABLE doctors ALTER COLUMN id RESTART WITH 100;

-- Weekly consulting hours, Monday (1) to Friday (5).
INSERT INTO doctor_availability (doctor_id, day_of_week, start_time, end_time,
                                 slot_duration_minutes, is_available, created_at)
SELECT d.id, dow.weekday, TIME '09:00:00', TIME '17:00:00', 30, TRUE, now()
FROM doctors d
CROSS JOIN (SELECT 1 AS weekday UNION ALL SELECT 2 UNION ALL SELECT 3
            UNION ALL SELECT 4 UNION ALL SELECT 5) dow;

-- The receptionist supports every doctor.
INSERT INTO user_doctor_mapping (user_id, doctor_id, hospital_id, relation_type, is_primary, created_at)
SELECT 5, d.id, 1, 'front_desk', FALSE, now() FROM doctors d;

-- ------------------------------------------------------------------------------ patients
INSERT INTO patients (id, hospital_id, patient_code, first_name, last_name, gender, date_of_birth,
                      blood_group, phone, email, address, emergency_contact_name,
                      emergency_contact_phone, status, created_at, updated_at, is_deleted) VALUES
    (1, 1, 'PAT-000001', 'Meera',   'Joshi',  'female', DATE '1991-04-12', 'O+',
     '+91 98800 20001', 'meera.joshi@example.com',   '48 Indiranagar, Bengaluru',  'Rakesh Joshi',  '+91 98800 30001', 'active', now(), now(), FALSE),
    (2, 1, 'PAT-000002', 'Rohan',   'Verma',  'male',   DATE '1986-09-03', 'B+',
     '+91 98800 20002', 'rohan.verma@example.com',   '9 Koramangala, Bengaluru',   'Sneha Verma',   '+91 98800 30002', 'active', now(), now(), FALSE),
    (3, 1, 'PAT-000003', 'Priya',   'Nair',   'female', DATE '1998-12-21', 'A-',
     '+91 98800 20003', 'priya.nair@example.com',    '221 Jayanagar, Bengaluru',   'Lakshmi Nair',  '+91 98800 30003', 'active', now(), now(), FALSE),
    (4, 1, 'PAT-000004', 'Devansh', 'Rao',    'male',   DATE '2015-06-08', 'AB+',
     '+91 98800 20004', 'devansh.rao@example.com',   '7 Whitefield, Bengaluru',    'Anil Rao',      '+91 98800 30004', 'active', now(), now(), FALSE),
    (5, 1, 'PAT-000005', 'Ishita',  'Kapoor', 'female', DATE '1975-03-17', 'O-',
     '+91 98800 20005', 'ishita.kapoor@example.com', '15 Malleshwaram, Bengaluru', 'Vivek Kapoor',  '+91 98800 30005', 'active', now(), now(), FALSE);

ALTER TABLE patients ALTER COLUMN id RESTART WITH 100;

INSERT INTO patient_medical_history (patient_id, condition_name, notes, recorded_by_doctor_id, recorded_at) VALUES
    (1, 'Hypertension',   'Stage 1. Monitored monthly; on low-dose medication.', 1, now() - INTERVAL '40' DAY),
    (2, 'Type 2 diabetes','HbA1c 7.1. Diet and metformin.',                      1, now() - INTERVAL '25' DAY),
    (3, 'Iron deficiency anaemia', 'Started oral iron; recheck in 8 weeks.',     2, now() - INTERVAL '12' DAY),
    (5, 'Osteoarthritis (right knee)', 'Physiotherapy twice weekly.',            3, now() - INTERVAL '6' DAY);

INSERT INTO patient_reports (patient_id, hospital_id, report_type, file_url, uploaded_by_user_id, uploaded_at) VALUES
    (1, 1, 'ECG',         '/reports/demo/meera-ecg.pdf',        2, now() - INTERVAL '38' DAY),
    (2, 1, 'Blood panel', '/reports/demo/rohan-blood.pdf',      6, now() - INTERVAL '20' DAY),
    (3, 1, 'Blood panel', '/reports/demo/priya-blood.pdf',      6, now() - INTERVAL '2' DAY);

-- -------------------------------------------------------------------------- appointments
INSERT INTO appointments (hospital_id, doctor_id, patient_id, appointment_mode, scheduled_at,
                          duration_minutes, status, queue_number, booked_by_user_id, reason,
                          consultation_fee, notes, created_at, updated_at) VALUES
    (1, 1, 1, 'online',  CURRENT_DATE + INTERVAL '9' HOUR  + INTERVAL '30' MINUTE, 30, 'confirmed',   1, 5, 'Blood pressure review',   1500.00, NULL, now(), now()),
    (1, 2, 4, 'walk_in', CURRENT_DATE + INTERVAL '10' HOUR + INTERVAL '15' MINUTE, 20, 'booked',      2, 5, 'Fever and cough',          900.00, NULL, now(), now()),
    (1, 3, 5, 'online',  CURRENT_DATE + INTERVAL '11' HOUR,                        30, 'checked_in',  3, 5, 'Knee pain follow-up',     1200.00, NULL, now(), now()),
    (1, 1, 2, 'online',  CURRENT_DATE + INTERVAL '13' HOUR + INTERVAL '45' MINUTE, 30, 'cancelled',   4, 5, 'Diabetes review',         1500.00, 'Patient rescheduled by phone', now(), now()),
    (1, 2, 3, 'online',  CURRENT_DATE + INTERVAL '15' HOUR + INTERVAL '20' MINUTE, 30, 'booked',      5, 5, 'Anaemia follow-up',        900.00, NULL, now(), now()),
    (1, 1, 1, 'online',  CURRENT_DATE - INTERVAL '2' DAY  + INTERVAL '10' HOUR,    30, 'completed',   1, 5, 'Cardiology consultation', 1500.00, NULL, now(), now()),
    (1, 3, 5, 'walk_in', CURRENT_DATE - INTERVAL '3' DAY  + INTERVAL '12' HOUR,    30, 'completed',   1, 5, 'Post-op review',          1200.00, NULL, now(), now()),
    (1, 2, 3, 'online',  CURRENT_DATE - INTERVAL '5' DAY  + INTERVAL '11' HOUR,    30, 'completed',   1, 5, 'Routine checkup',          900.00, NULL, now(), now()),
    (1, 1, 2, 'online',  CURRENT_DATE + INTERVAL '2' DAY  + INTERVAL '9' HOUR,     30, 'booked',      1, 5, 'Diabetes review',         1500.00, NULL, now(), now());

-- ------------------------------------------------------------------------- prescriptions
INSERT INTO prescriptions (hospital_id, appointment_id, doctor_id, patient_id, diagnosis,
                           medicines_json, digitally_signed, signed_at, status, created_at, updated_at)
VALUES
    (1, NULL, 1, 1, 'Essential hypertension, stable',
     '[{"medicationName":"Telmisartan 40mg","dosage":"1 tablet","frequency":"Once daily","durationDays":30,"instructions":"After breakfast"},{"medicationName":"Aspirin 75mg","dosage":"1 tablet","frequency":"Once daily","durationDays":30,"instructions":"After dinner"}]',
     TRUE, now() - INTERVAL '2' DAY, 'active', now() - INTERVAL '2' DAY, now() - INTERVAL '2' DAY),
    (1, NULL, 2, 3, 'Iron deficiency anaemia',
     '[{"medicationName":"Ferrous ascorbate 100mg","dosage":"1 tablet","frequency":"Twice daily","durationDays":56,"instructions":"With vitamin C"}]',
     TRUE, now() - INTERVAL '5' DAY, 'active', now() - INTERVAL '5' DAY, now() - INTERVAL '5' DAY),
    (1, NULL, 3, 5, 'Osteoarthritis, right knee',
     '[{"medicationName":"Paracetamol 650mg","dosage":"1 tablet","frequency":"Thrice daily","durationDays":7,"instructions":"After meals"}]',
     TRUE, now() - INTERVAL '3' DAY, 'completed', now() - INTERVAL '3' DAY, now() - INTERVAL '1' DAY);

-- ------------------------------------------------------------------------------ pharmacy
INSERT INTO medications (hospital_id, name, category, unit_price, stock_quantity, reorder_level,
                         expiry_date, created_at, updated_at) VALUES
    (1, 'Amoxicillin 500mg',        'Antibiotic',     12.50, 18,  40, CURRENT_DATE + INTERVAL '300' DAY, now(), now()),
    (1, 'Paracetamol 650mg',        'Analgesic',       2.20, 640, 150, CURRENT_DATE + INTERVAL '540' DAY, now(), now()),
    (1, 'Telmisartan 40mg',         'Antihypertensive',8.90, 220, 80, CURRENT_DATE + INTERVAL '420' DAY, now(), now()),
    (1, 'Metformin 500mg',          'Antidiabetic',    3.40, 310, 100, CURRENT_DATE + INTERVAL '380' DAY, now(), now()),
    (1, 'Ferrous ascorbate 100mg',  'Haematinic',      6.75, 54,  60, CURRENT_DATE + INTERVAL '260' DAY, now(), now()),
    (1, 'Salbutamol inhaler',       'Respiratory',   210.00, 12,  20, CURRENT_DATE + INTERVAL '200' DAY, now(), now());

-- ---------------------------------------------------------------------------- laboratory
INSERT INTO lab_orders (hospital_id, patient_id, doctor_id, test_name, priority, status,
                        result_summary, ordered_at, completed_at, updated_at) VALUES
    (1, 3, 2, 'Complete blood count', 'ROUTINE', 'COMPLETED',
     'Hb 9.8 g/dL, MCV 71 fL — microcytic anaemia.', now() - INTERVAL '3' DAY, now() - INTERVAL '2' DAY, now() - INTERVAL '2' DAY),
    (1, 1, 1, 'Lipid profile',        'ROUTINE', 'IN_PROGRESS', NULL, now() - INTERVAL '1' DAY, NULL, now()),
    (1, 2, 1, 'HbA1c',                'URGENT',  'ORDERED',     NULL, now() - INTERVAL '4' HOUR, NULL, now()),
    (1, 5, 3, 'Knee X-ray',           'ROUTINE', 'COMPLETED',
     'Moderate medial joint space narrowing.', now() - INTERVAL '6' DAY, now() - INTERVAL '5' DAY, now() - INTERVAL '5' DAY);

-- ------------------------------------------------------------------------- notifications
INSERT INTO notifications (hospital_id, category, severity, title, message, is_read, created_at) VALUES
    (1, 'LABORATORY',  'INFO',    'Lab results ready', 'Priya Nair''s blood panel just came in.',              FALSE, now() - INTERVAL '2' HOUR),
    (1, 'APPOINTMENT', 'WARNING', 'Schedule conflict', 'Dr. Kabir Shah has two bookings at 2:00 PM.',          FALSE, now() - INTERVAL '3' HOUR),
    (1, 'PHARMACY',    'WARNING', 'Low stock alert',   'Pharmacy flagged Amoxicillin 500mg running low.',      FALSE, now() - INTERVAL '5' HOUR),
    (1, 'APPOINTMENT', 'INFO',    'Appointment booked','Devansh Rao is scheduled with Dr. Sana Iyer today.',   TRUE,  now() - INTERVAL '1' DAY);

-- ------------------------------------------------------------------------------ settings
INSERT INTO workspace_settings (hospital_id, setting_key, setting_value, updated_at) VALUES
    (1, 'organization.name',               'MedFlow Clinic', now()),
    (1, 'organization.tagline',            'The clinical operating system for modern care teams', now()),
    (1, 'appearance.theme',                'light', now()),
    (1, 'scheduling.slot-duration-minutes','30', now()),
    (1, 'security.session-timeout-minutes','480', now());

-- --------------------------------------------------------------------------------- audit
INSERT INTO audit_logs (hospital_id, user_id, action, entity_type, entity_id, metadata_json,
                        ip_address, created_at) VALUES
    (1, 1, 'WORKSPACE_SEEDED', 'hospital', 1, '{"source":"demo-seed"}', '127.0.0.1', now());
