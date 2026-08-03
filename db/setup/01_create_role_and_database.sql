-- =====================================================================================
-- OPTIONAL — a dedicated application role instead of running as "postgres".
--
-- Run connected to the "postgres" database as a superuser. If you already created the
-- database with 00_create_database.sql, only the CREATE ROLE and ALTER DATABASE lines
-- are needed.
--
-- Afterwards point the application at it:
--     $env:DB_USERNAME = "medflow"; $env:DB_PASSWORD = "medflow"
--
-- Then run 02_grants.sql while connected to the medflow database.
-- =====================================================================================

CREATE ROLE medflow WITH LOGIN PASSWORD 'medflow';

-- Skip this if 00_create_database.sql already ran.
CREATE DATABASE medflow
    WITH OWNER = medflow
         ENCODING = 'UTF8'
         TEMPLATE = template0;

-- If the database already existed, hand it over instead:
-- ALTER DATABASE medflow OWNER TO medflow;
