-- =====================================================================================
-- Ops script: turn a role's WRITE (update) access to a module on or off.
--
-- This is a plain SQL script you run by hand with psql — it is NOT a Flyway migration
-- and must never be dropped into src/main/resources/db/migration. It only touches
-- role_permissions rows that V15__enable_billing_inpatient_modules.sql already seeded
-- (globally, for every role, hospital_id IS NULL); it never removes a permission or
-- module from the catalogue.
--
-- "Stop update" here means: the role keeps `<module>:read` (can still view Billing &
-- Invoicing / Inpatient & Wards) but loses `<module>:write`, so create/update/delete
-- calls for that module are rejected for anyone with only that role. Read access is
-- left untouched unless you explicitly revoke it too (see the last section).
--
-- Usage (psql):
--   psql -h <host> -U <user> -d <database> \
--        -v role_code="'NURSE'" -v module_code="'billing'" \
--        -f scripts/manage-role-permissions.sql
--
-- Or open this file in an SQL client, edit the two \set lines below, and run the
-- section you need — everything is commented out except the "current state" query,
-- so nothing changes until you uncomment a block.
-- =====================================================================================

-- Edit these two, or pass -v role_code=... -v module_code=... on the psql command line.
-- role_code   is one of: ADMIN, DOCTOR, NURSE, LAB_TECHNICIAN, PHARMACIST, RECEPTIONIST, PATIENT
-- module_code is one of: billing, inpatient (or any module_code in modules_master)
\set role_code   '''NURSE'''
\set module_code '''billing'''

-- -------------------------------------------------------------------------------------
-- 1. See what this role can currently do on this module (safe to run any time).
-- -------------------------------------------------------------------------------------
SELECT r.role_code, p.permission_code, p.permission_name, rp.hospital_id
FROM role_permissions rp
JOIN roles r ON r.id = rp.role_id
JOIN permissions p ON p.id = rp.permission_id
JOIN modules_master m ON m.id = p.module_id
WHERE r.role_code = :role_code
  AND m.module_code = :module_code
ORDER BY p.permission_code;

-- -------------------------------------------------------------------------------------
-- 2. STOP UPDATES for this role on this module (revokes "<module_code>:write" only;
--    read access is untouched). Uncomment to run.
-- -------------------------------------------------------------------------------------
-- DELETE FROM role_permissions rp
-- USING roles r, permissions p, modules_master m
-- WHERE rp.role_id = r.id
--   AND rp.permission_id = p.id
--   AND p.module_id = m.id
--   AND r.role_code = :role_code
--   AND m.module_code = :module_code
--   AND p.permission_code = :module_code || ':write'
--   AND rp.hospital_id IS NULL;   -- global default grant; see the tenant note below

-- -------------------------------------------------------------------------------------
-- 3. RESTORE update access for this role on this module (undoes step 2).
-- -------------------------------------------------------------------------------------
-- INSERT INTO role_permissions (role_id, permission_id, hospital_id, created_at)
-- SELECT r.id, p.id, NULL, now()
-- FROM roles r
-- JOIN permissions p ON p.permission_code = :module_code || ':write'
-- WHERE r.role_code = :role_code
--   AND NOT EXISTS (
--     SELECT 1 FROM role_permissions rp
--     WHERE rp.role_id = r.id AND rp.permission_id = p.id AND rp.hospital_id IS NULL
--   );

-- -------------------------------------------------------------------------------------
-- 4. Fully lock this role out of the module (revokes BOTH read and write). Uncomment
--    to run — more than "stop update", this hides the module from that role entirely.
-- -------------------------------------------------------------------------------------
-- DELETE FROM role_permissions rp
-- USING roles r, permissions p, modules_master m
-- WHERE rp.role_id = r.id
--   AND rp.permission_id = p.id
--   AND p.module_id = m.id
--   AND r.role_code = :role_code
--   AND m.module_code = :module_code
--   AND rp.hospital_id IS NULL;

-- -------------------------------------------------------------------------------------
-- On hospital_id / tenant scoping: RolePermissionRepository.findPermissionCodes()
-- resolves a role's permissions as `hospital_id IS NULL OR hospital_id = :hospitalId`
-- — i.e. global rows and hospital-scoped rows are ADDITIVE, not an override. A
-- hospital-scoped row can only GRANT a permission on top of the global default; it
-- cannot be used to restrict one hospital while every other hospital keeps write
-- access. If you need "every hospital except X" or "only hospital X", that needs the
-- global row deleted (as in step 2/4 above, which then applies everywhere) plus new
-- hospital-scoped INSERTs for whichever hospitals should still have it — there is no
-- per-hospital "deny" today.
-- =====================================================================================
