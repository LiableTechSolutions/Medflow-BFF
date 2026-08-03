-- =====================================================================================
-- DESTRUCTIVE — wipes every MedFlow table so Flyway rebuilds the schema (and the demo
-- data) on the next application start. Use when a migration was edited after it had
-- already been applied, which Flyway reports as a checksum mismatch.
--
-- Run connected to the "medflow" database. Stop the application first.
-- =====================================================================================

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- Default setup (application connects as postgres):
GRANT ALL ON SCHEMA public TO postgres;

-- Using the optional dedicated role instead? Also run:
-- GRANT ALL ON SCHEMA public TO medflow;
-- ALTER SCHEMA public OWNER TO medflow;
