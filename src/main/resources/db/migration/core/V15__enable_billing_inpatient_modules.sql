-- =====================================================================================
-- Turns on the two Phase-2 modules — "Billing & Invoicing" and "Inpatient & Wards" —
-- for every user, for now: the module catalogue flag, a read/write permission pair for
-- each, those permissions granted to every seeded role, and portal visibility for every
-- portal group (staff, doctor and patient).
--
-- This is intentionally broad. To scope write access down to specific roles later
-- (without touching the schema again), use scripts/manage-role-permissions.sql at the
-- repo root — it is a plain ops script, not a migration, meant to be run by hand.
-- =====================================================================================

UPDATE modules_master SET is_active = TRUE WHERE module_code IN ('billing', 'inpatient');

INSERT INTO permissions (permission_code, module_id, permission_name, description)
SELECT v.permission_code, m.id, v.permission_name, v.description
FROM (VALUES
    ('billing:read',    'billing',   'View billing',      'Read invoices, payments and insurance claims'),
    ('billing:write',   'billing',   'Manage billing',     'Create and update invoices, payments and insurance claims'),
    ('inpatient:read',  'inpatient', 'View inpatients',    'Read admissions, beds and ward rounds'),
    ('inpatient:write', 'inpatient', 'Manage inpatients',  'Admit, discharge and manage beds and ward rounds')
) AS v(permission_code, module_code, permission_name, description)
JOIN modules_master m ON m.module_code = v.module_code;

-- Every seeded role gets both read and write, for now — see the ops script above to
-- later revoke ':write' for a specific role without another migration.
INSERT INTO role_permissions (role_id, permission_id, hospital_id, created_at)
SELECT r.id, p.id, NULL, now()
FROM roles r
CROSS JOIN permissions p
WHERE p.permission_code IN ('billing:read', 'billing:write', 'inpatient:read', 'inpatient:write');

-- Every portal (staff, doctor, patient) can see both modules, for now.
INSERT INTO user_group_portal_access (user_group_id, module_id, can_access)
SELECT g.id, m.id, TRUE
FROM user_groups g
CROSS JOIN modules_master m
WHERE m.module_code IN ('billing', 'inpatient');
