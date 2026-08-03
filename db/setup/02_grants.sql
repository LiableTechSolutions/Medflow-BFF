-- =====================================================================================
-- Run this connected to the "medflow" DATABASE (not "postgres"), as a superuser.
--
-- PostgreSQL 15 and later no longer let every role create objects in the public schema,
-- so the application role needs this grant before Flyway can create its tables.
-- =====================================================================================

GRANT ALL ON SCHEMA public TO medflow;
ALTER SCHEMA public OWNER TO medflow;

-- Verify: should print "medflow".
SELECT current_database() AS database, schema_owner
FROM information_schema.schemata
WHERE schema_name = 'public';
