-- =====================================================================================
-- MedFlow AI — the only step needed before the first run.
--
-- Run ONCE, connected to the "postgres" maintenance database as a superuser
-- (pgAdmin: right-click the "postgres" database → Query Tool → paste → F5).
--
-- Matches the application defaults in application.yml:
--     jdbc:postgresql://localhost:5432/medflow   user: postgres
--
-- Flyway creates all 24 tables and seeds a demo hospital on first startup — do not
-- create tables by hand.
-- =====================================================================================

CREATE DATABASE medflow ENCODING 'UTF8' TEMPLATE template0;

-- Verify (run after switching the Query Tool to the medflow database):
--   SELECT current_database();
