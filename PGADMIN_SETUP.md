# Setting up the MedFlow database in pgAdmin

How to create the local PostgreSQL database MedFlow AI runs against, let the application
build the tables, and inspect or extend the data afterwards.

Written for **PostgreSQL 17 + pgAdmin 4 on Windows**, and verified end to end on this
machine — the steps below are exactly what was run to produce a working install.

| Thing | Path / value |
|---|---|
| PostgreSQL install | `C:\Program Files\PostgreSQL\17` |
| pgAdmin 4 | `C:\Program Files\PostgreSQL\17\pgAdmin 4\runtime\pgAdmin4.exe` |
| `psql` (CLI) | `C:\Program Files\PostgreSQL\17\bin\psql.exe` |
| Windows service | `postgresql-x64-17` |
| Server | `localhost:5432` |

What you end up with:

| Setting | Value used by the app |
|---|---|
| Database | `medflow` |
| User | `postgres` |
| Password | `newpassword` |
| JDBC URL | `jdbc:postgresql://localhost:5432/medflow` |

> **You never create tables by hand.** Flyway builds all 24 tables and seeds a demo
> hospital the first time the application starts.

---

## Step 0 — Check the server is running

Open **Services** (`Win + R` → `services.msc`) and confirm **postgresql-x64-17** is
*Running*, or:

```powershell
Get-Service postgresql-x64-17
```

---

## Step 1 — Connect pgAdmin to the server

1. Start **pgAdmin 4** from the Start menu (the first launch takes ~30 seconds).
2. If it asks for a **master password**, set one. It only protects pgAdmin's own saved
   passwords — it is not a database password.
3. Expand **Servers** in the left-hand Browser tree.
   * A server named `PostgreSQL 17` is usually already registered — click it and enter
     the `postgres` password (`newpassword` on this machine).
   * If nothing is listed, right-click **Servers → Register → Server…**:

     | Tab | Field | Value |
     |---|---|---|
     | General | Name | `PostgreSQL 17` |
     | Connection | Host name/address | `localhost` |
     | Connection | Port | `5432` |
     | Connection | Maintenance database | `postgres` |
     | Connection | Username | `postgres` |
     | Connection | Password | `newpassword` |
     | Connection | Save password | ✔ |

> **Forgot the `postgres` password?** Edit
> `C:\Program Files\PostgreSQL\17\data\pg_hba.conf`, change the two `127.0.0.1/32` and
> `::1/128` lines from `scram-sha-256` to `trust`, restart the `postgresql-x64-17`
> service, connect with no password, run `ALTER USER postgres WITH PASSWORD 'newpassword';`,
> then set `scram-sha-256` back and restart again.

---

## Step 2 — Create the database

1. Select the **`postgres`** database in the tree
   (Servers → PostgreSQL 17 → Databases → postgres).
2. **Tools → Query Tool**.
3. Run:

```sql
CREATE DATABASE medflow ENCODING 'UTF8' TEMPLATE template0;
```

That is the whole database setup when you connect as `postgres` (the default in
`application.yml`). Refresh **Databases** and `medflow` appears, empty.

Clicking instead of typing: right-click **Databases → Create → Database…**, name it
`medflow`, **Save**.

### Optional — a dedicated application role

Running the app as the superuser is fine locally, but a separate role is closer to
production. Run in the **postgres** database:

```sql
CREATE ROLE medflow WITH LOGIN PASSWORD 'medflow';
ALTER DATABASE medflow OWNER TO medflow;
```

then, connected to the **medflow** database (PostgreSQL 15+ requires this or Flyway
fails with `permission denied for schema public`):

```sql
GRANT ALL ON SCHEMA public TO medflow;
ALTER SCHEMA public OWNER TO medflow;
```

and point the app at it:

```powershell
$env:DB_USERNAME = "medflow"; $env:DB_PASSWORD = "medflow"
```

Both variants are scripted in [`db/setup/`](db/setup):

| Script | Run it against | What it does |
|---|---|---|
| `00_create_database.sql` | `postgres` | Creates the `medflow` database (the default path) |
| `01_create_role_and_database.sql` | `postgres` | Optional dedicated `medflow` role + database |
| `02_grants.sql` | `medflow` | Schema grants needed by that role on PostgreSQL 15+ |
| `03_sample_test_data.sql` | `medflow` | Extra patients, appointments and stock on top of the demo seed |
| `99_reset_database.sql` | `medflow` | Drops everything so Flyway can rebuild |

Open any of them with **Query Tool → Open File** (folder icon) and press **F5**.

---

## Step 3 — Let the application create the tables

```powershell
cd C:\Users\PCP\Downloads\medflow-ai\medflow-ai
mvn spring-boot:run
```

The log shows Flyway building the schema:

```
Migrating schema "public" to version "1 - hospitals and modules"
Migrating schema "public" to version "2 - rbac"
...
Migrating schema "public" to version "100 - demo workspace"
Successfully applied 15 migrations to schema "public", now at version v100
Started MedflowApplication in 30.7 seconds
```

Nothing else is needed — schema and test data are both in place.

---

## Step 4 — Verify in pgAdmin

Right-click **medflow → Refresh**, then expand **Schemas → public → Tables**. You get 25
entries: 24 application tables plus Flyway's `flyway_schema_history`.

| Area | Tables |
|---|---|
| Tenancy | `hospitals`, `modules_master`, `hospital_module_entitlements` |
| Access control | `roles`, `permissions`, `role_permissions`, `user_groups`, `user_group_portal_access` |
| People | `users`, `doctors`, `doctor_availability`, `user_doctor_mapping`, `patients`, `patient_medical_history`, `patient_reports`, `user_patient_mapping` |
| Clinical | `appointments`, `prescriptions`, `lab_orders`, `medications` |
| Platform | `notifications`, `chat_messages`, `workspace_settings`, `audit_logs` |

Row counts after a fresh start (this is the seeded demo workspace):

```sql
SELECT 'hospitals' t, count(*) FROM hospitals
UNION ALL SELECT 'users',         count(*) FROM users          -- 6
UNION ALL SELECT 'doctors',       count(*) FROM doctors        -- 3
UNION ALL SELECT 'patients',      count(*) FROM patients       -- 5
UNION ALL SELECT 'appointments',  count(*) FROM appointments   -- 9
UNION ALL SELECT 'prescriptions', count(*) FROM prescriptions  -- 3
UNION ALL SELECT 'lab_orders',    count(*) FROM lab_orders     -- 4
UNION ALL SELECT 'medications',   count(*) FROM medications    -- 6
UNION ALL SELECT 'notifications', count(*) FROM notifications  -- 4
UNION ALL SELECT 'roles',         count(*) FROM roles          -- 7
UNION ALL SELECT 'permissions',   count(*) FROM permissions    -- 21
ORDER BY 1;
```

Other queries worth keeping:

```sql
-- Which migrations ran, and did they succeed?
SELECT installed_rank, version, description, success
FROM flyway_schema_history ORDER BY installed_rank;

-- Staff with their role
SELECT u.id, u.first_name, u.last_name, u.email, r.role_code, u.status
FROM users u JOIN roles r ON r.id = u.role_id ORDER BY u.id;

-- Today's schedule, joined the way the dashboard reads it
SELECT a.queue_number, a.scheduled_at, a.status, a.appointment_mode,
       p.first_name || ' ' || p.last_name AS patient,
       du.first_name || ' ' || du.last_name AS doctor
FROM appointments a
JOIN patients p ON p.id = a.patient_id
JOIN doctors  d ON d.id = a.doctor_id
JOIN users   du ON du.id = d.user_id
WHERE a.scheduled_at::date = CURRENT_DATE
ORDER BY a.queue_number;
```

Browse without SQL: right-click a table → **View/Edit Data → All Rows**. To see a
table's columns, constraints and indexes, select it and open the **SQL** tab.

---

## Step 5 — Sign in

The demo workspace ships with these accounts:

| Account | Email | Password | Role |
|---|---|---|---|
| Administrator | `admin@medflow.local` | `Admin@12345` | ADMIN |
| Doctor | `kabir@medflow.local` | `Doctor@12345` | DOCTOR |
| Doctor | `sana@medflow.local` | `Doctor@12345` | DOCTOR |
| Doctor | `arjun@medflow.local` | `Doctor@12345` | DOCTOR |
| Receptionist | `reception@medflow.local` | `Doctor@12345` | RECEPTIONIST |
| Lab technician | `lab@medflow.local` | `Doctor@12345` | LAB_TECHNICIAN |

Use them in the UI (<http://localhost:5173>) or Swagger UI
(<http://localhost:8080/swagger-ui.html>).

---

## Adding your own test data

### Through the API (recommended)

The API generates codes (`PAT-000006`), hashes passwords, assigns queue numbers and
stamps the tenant for you — SQL inserts have to do all of that by hand.

```powershell
$login = Invoke-RestMethod -Uri http://localhost:8080/api/v1/auth/login -Method Post `
  -ContentType 'application/json' `
  -Body '{"email":"admin@medflow.local","password":"Admin@12345"}'
$h = @{ Authorization = "Bearer $($login.data.accessToken)" }

# A patient
Invoke-RestMethod -Uri http://localhost:8080/api/v1/patients -Method Post -Headers $h `
  -ContentType 'application/json' `
  -Body '{"firstName":"Aarav","lastName":"Menon","gender":"MALE","dateOfBirth":"1988-02-14","bloodGroup":"B+","phone":"+91 90000 11111"}'

# A doctor (also creates their login)
Invoke-RestMethod -Uri http://localhost:8080/api/v1/doctors -Method Post -Headers $h `
  -ContentType 'application/json' `
  -Body '{"firstName":"Neha","lastName":"Gupta","email":"neha@medflow.local","password":"Doctor@12345","specialty":"Dermatology","qualification":"MBBS, MD","registrationNumber":"KMC-556677","yearsOfExperience":9,"consultationFee":1000}'

# An appointment for patient 1 with doctor 1
Invoke-RestMethod -Uri http://localhost:8080/api/v1/appointments -Method Post -Headers $h `
  -ContentType 'application/json' `
  -Body '{"patientId":1,"doctorId":1,"scheduledAt":"2026-08-10T09:30:00Z","appointmentMode":"ONLINE","reason":"Follow-up"}'
```

### Through SQL

For bulk data, [`db/setup/03_sample_test_data.sql`](db/setup/03_sample_test_data.sql)
adds more patients, a week of appointments and extra stock to the demo hospital. Open it
in the Query Tool against the **medflow** database and press F5. It is safe to run more
than once — each run appends a new batch.

Two rules when writing your own inserts:

* every row needs the right `hospital_id` (the demo hospital is `1`);
* enum-typed columns use the lower-case values from the design — `status = 'active'`,
  `appointment_mode = 'walk_in'`, `gender = 'female'`. A `CHECK` constraint rejects
  anything else.

---

## Resetting the database

Editing a migration that has already run makes Flyway refuse to start:

```
Validate failed: Migration checksum mismatch for migration version 5
```

Rebuild from scratch — Query Tool on the **medflow** database:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
```

(also in [`db/setup/99_reset_database.sql`](db/setup/99_reset_database.sql)). Restart the
application and the schema plus demo data are recreated.

To drop the whole database, right-click **medflow → Delete/Drop** — close any Query Tool
tabs against it first, or PostgreSQL refuses while sessions remain open.

---

## Backups

* **Export**: right-click `medflow` → **Backup…** → filename → Format *Custom* → **Backup**.
* **Import**: right-click → **Restore…** → pick the file → **Restore**.

If pgAdmin cannot find the tools, set **File → Preferences → Paths → Binary paths →
PostgreSQL 17** to `C:\Program Files\PostgreSQL\17\bin`.

---

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| `Connection refused` | Service not running — start `postgresql-x64-17`. |
| `password authentication failed for user "postgres"` | Wrong password. Set `DB_PASSWORD` to the real one, or reset it with the `pg_hba.conf` trick above. |
| `database "medflow" does not exist` | Step 2 was skipped, or it ran on a different server. |
| `permission denied for schema public` | Only when using the optional `medflow` role — run `02_grants.sql` while connected to the `medflow` database. |
| `Unsupported Database: PostgreSQL 17` | `flyway-database-postgresql` missing from the classpath. It is in this repository's `pom.xml`; run `mvn clean package`. |
| `Validate failed: checksum mismatch` | A migration changed after running — reset with `99_reset_database.sql`. |
| Tables missing after startup | Flyway failed; the app refuses to start on a broken migration, so read the first error in the log. |
| `FATAL: sorry, too many clients already` | Close idle Query Tool tabs, or raise `max_connections` in `postgresql.conf`. |
