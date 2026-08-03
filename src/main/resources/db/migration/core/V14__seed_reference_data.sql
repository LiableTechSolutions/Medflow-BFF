-- =====================================================================================
-- Platform reference data: the module catalogue, roles, permissions and portal groups.
-- These rows are identical for every tenant, so they ship with the schema rather than
-- being created at runtime. Identity counters are restarted past the seeded ids.
-- =====================================================================================

INSERT INTO modules_master (id, module_code, module_name, phase, description, is_active) VALUES
    (1,  'dashboard',     'Dashboard',              1, 'Clinic-wide KPIs, activity chart and today''s schedule', TRUE),
    (2,  'doctors',       'Doctor Management',      1, 'Roster, credentials, schedules and availability',        TRUE),
    (3,  'patients',      'Patient Management',     1, 'Registration, medical history and reports',              TRUE),
    (4,  'appointments',  'Appointment Management', 1, 'Booking, queue, reschedule and consultation flow',       TRUE),
    (5,  'prescriptions', 'Prescription Management',1, 'Digitally signed prescriptions and history',             TRUE),
    (6,  'laboratory',    'Laboratory',             1, 'Test orders, processing queue and results',              TRUE),
    (7,  'pharmacy',      'Pharmacy',               1, 'Medication catalogue, stock and reorder alerts',         TRUE),
    (8,  'reports',       'Reports & Analytics',    1, 'Operational and revenue analytics',                      TRUE),
    (9,  'notifications', 'Notifications',          1, 'Workspace alert feed',                                   TRUE),
    (10, 'ai_assistant',  'AI Assistant',           1, 'Conversational clinical assistant',                      TRUE),
    (11, 'users',         'User Management',        1, 'Staff accounts, roles and access',                       TRUE),
    (12, 'settings',      'Settings',               1, 'Workspace configuration',                                TRUE),
    (13, 'billing',       'Billing & Invoicing',    2, 'Invoices, payments and insurance claims',                FALSE),
    (14, 'inpatient',     'Inpatient & Wards',      2, 'Admissions, beds and ward rounds',                       FALSE);

ALTER TABLE modules_master ALTER COLUMN id RESTART WITH 100;

INSERT INTO roles (id, role_code, role_name, description, is_system_role, created_at) VALUES
    (1, 'ADMIN',          'Administrator',    'Full access to the hospital workspace',        TRUE, now()),
    (2, 'DOCTOR',         'Doctor',           'Clinical access: patients, appointments, prescriptions', TRUE, now()),
    (3, 'NURSE',          'Nurse',            'Patient care support and vitals',              TRUE, now()),
    (4, 'LAB_TECHNICIAN', 'Lab Technician',   'Laboratory queue and results',                 TRUE, now()),
    (5, 'PHARMACIST',     'Pharmacist',       'Pharmacy catalogue and dispensing',            TRUE, now()),
    (6, 'RECEPTIONIST',   'Receptionist',     'Front desk: registration and scheduling',      TRUE, now()),
    (7, 'PATIENT',        'Patient',          'Self-service portal account',                  TRUE, now());

ALTER TABLE roles ALTER COLUMN id RESTART WITH 100;

INSERT INTO permissions (id, permission_code, module_id, permission_name, description) VALUES
    (1,  'dashboard:read',      1,  'View dashboard',        'Read clinic KPIs and activity'),
    (2,  'doctors:read',        2,  'View doctors',          'Browse the doctor roster'),
    (3,  'doctors:write',       2,  'Manage doctors',        'Onboard and update doctors and schedules'),
    (4,  'patients:read',       3,  'View patients',         'Browse patient records'),
    (5,  'patients:write',      3,  'Manage patients',       'Register and update patients, history and reports'),
    (6,  'appointments:read',   4,  'View appointments',     'Browse the schedule'),
    (7,  'appointments:write',  4,  'Manage appointments',   'Book, reschedule and progress appointments'),
    (8,  'prescriptions:read',  5,  'View prescriptions',    'Read prescription history'),
    (9,  'prescriptions:write', 5,  'Issue prescriptions',   'Write and sign prescriptions'),
    (10, 'laboratory:read',     6,  'View lab orders',       'Read the laboratory queue'),
    (11, 'laboratory:write',    6,  'Manage lab orders',     'Order tests and record results'),
    (12, 'pharmacy:read',       7,  'View pharmacy',         'Read the medication catalogue'),
    (13, 'pharmacy:write',      7,  'Manage pharmacy',       'Maintain catalogue and stock levels'),
    (14, 'reports:read',        8,  'View reports',          'Read analytics and reports'),
    (15, 'notifications:read',  9,  'View notifications',    'Read the alert feed'),
    (16, 'assistant:use',       10, 'Use AI assistant',      'Converse with the clinical assistant'),
    (17, 'users:read',          11, 'View users',            'Browse staff accounts'),
    (18, 'users:write',         11, 'Manage users',          'Invite staff and change roles or status'),
    (19, 'settings:read',       12, 'View settings',         'Read workspace configuration'),
    (20, 'settings:write',      12, 'Manage settings',       'Change workspace configuration'),
    (21, 'audit:read',          11, 'View audit log',        'Read the workspace audit trail');

ALTER TABLE permissions ALTER COLUMN id RESTART WITH 100;

-- ADMIN: everything.
INSERT INTO role_permissions (role_id, permission_id, hospital_id, created_at)
SELECT 1, p.id, NULL, now() FROM permissions p;

-- DOCTOR: clinical workflow, read-only administration.
INSERT INTO role_permissions (role_id, permission_id, hospital_id, created_at)
SELECT 2, p.id, NULL, now() FROM permissions p
WHERE p.permission_code IN ('dashboard:read', 'doctors:read', 'patients:read', 'patients:write',
                            'appointments:read', 'appointments:write', 'prescriptions:read',
                            'prescriptions:write', 'laboratory:read', 'laboratory:write',
                            'pharmacy:read', 'reports:read', 'notifications:read', 'assistant:use');

-- NURSE
INSERT INTO role_permissions (role_id, permission_id, hospital_id, created_at)
SELECT 3, p.id, NULL, now() FROM permissions p
WHERE p.permission_code IN ('dashboard:read', 'doctors:read', 'patients:read', 'patients:write',
                            'appointments:read', 'appointments:write', 'prescriptions:read',
                            'laboratory:read', 'notifications:read', 'assistant:use');

-- LAB_TECHNICIAN
INSERT INTO role_permissions (role_id, permission_id, hospital_id, created_at)
SELECT 4, p.id, NULL, now() FROM permissions p
WHERE p.permission_code IN ('dashboard:read', 'patients:read', 'laboratory:read',
                            'laboratory:write', 'notifications:read');

-- PHARMACIST
INSERT INTO role_permissions (role_id, permission_id, hospital_id, created_at)
SELECT 5, p.id, NULL, now() FROM permissions p
WHERE p.permission_code IN ('dashboard:read', 'patients:read', 'prescriptions:read',
                            'pharmacy:read', 'pharmacy:write', 'notifications:read');

-- RECEPTIONIST
INSERT INTO role_permissions (role_id, permission_id, hospital_id, created_at)
SELECT 6, p.id, NULL, now() FROM permissions p
WHERE p.permission_code IN ('dashboard:read', 'doctors:read', 'patients:read', 'patients:write',
                            'appointments:read', 'appointments:write', 'notifications:read');

-- PATIENT
INSERT INTO role_permissions (role_id, permission_id, hospital_id, created_at)
SELECT 7, p.id, NULL, now() FROM permissions p
WHERE p.permission_code IN ('appointments:read', 'prescriptions:read', 'notifications:read');

INSERT INTO user_groups (id, group_code, group_name, description, is_active) VALUES
    (1, 'STAFF_PORTAL',   'Staff Portal',   'Front desk, administration and support staff', TRUE),
    (2, 'DOCTOR_PORTAL',  'Doctor Portal',  'Consulting doctors',                           TRUE),
    (3, 'PATIENT_PORTAL', 'Patient Portal', 'Patients and their caretakers',                TRUE);

ALTER TABLE user_groups ALTER COLUMN id RESTART WITH 100;

-- Staff portal sees every phase-1 module.
INSERT INTO user_group_portal_access (user_group_id, module_id, can_access)
SELECT 1, m.id, TRUE FROM modules_master m WHERE m.phase = 1;

-- Doctor portal: clinical modules only.
INSERT INTO user_group_portal_access (user_group_id, module_id, can_access)
SELECT 2, m.id, TRUE FROM modules_master m
WHERE m.module_code IN ('dashboard', 'patients', 'appointments', 'prescriptions', 'laboratory',
                        'reports', 'notifications', 'ai_assistant');

-- Patient portal: their own appointments and prescriptions.
INSERT INTO user_group_portal_access (user_group_id, module_id, can_access)
SELECT 3, m.id, TRUE FROM modules_master m
WHERE m.module_code IN ('appointments', 'prescriptions', 'notifications', 'ai_assistant');
