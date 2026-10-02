# MedFlow enterprise HMS — build prompt

Paste this at the start of a session (or alongside a single module's section) to build the next piece of MedFlow to the same standard every time.

## Context

MedFlow is an **enterprise-level hospital management system**, not a demo.

- Backend: `Medflow-BFF` — Spring Boot 3 + Spring Modulith. One package per module under `com.medflow.modules.<module>` with `api` (public contract: service interface, requests, responses, events), `application` (service impl), `controller`, `domain` (entities, repositories). Modules talk only through another module's `api` package or domain events; `ModularityTest` enforces this.
- Frontend: `Medflow-UI-Repo/medflow-ai` — React + TypeScript. Reusable pieces live in `src/shared/components/<Name>/` with a Storybook story; pages live in `src/modules/<module>/pages/`.
- Multi-tenant: every row carries `hospital_id`; services always take `hospitalId` from `TenantContext`, never from the client. A different hospital's id must behave like "not found" (404), not "forbidden".
- Roles (Spring `hasRole`): `ADMIN`, `DOCTOR`, `NURSE`, `LAB_TECHNICIAN`, `PHARMACIST`, `RECEPTIONIST`, `PATIENT`. Permissions `billing:*` and `inpatient:*` are seeded but not yet enforced in code — enforce by role as the existing modules do.
- Migrations: Flyway, `src/main/resources/db/migration/<module>/V<n>__name.sql`, versions are global and increase (next free: **V109**). Must run on both PostgreSQL and the H2 used by tests.
- Errors: `ResourceNotFoundException` → 404, `DuplicateResourceException` → 409, `BusinessRuleViolationException` → 422, bean validation → 400, no token → 401, wrong role → 403.
- Notifications: modules never call the notifications module; they publish an event and `NotificationEventListener` reacts (email / WhatsApp / SMS senders already exist and degrade to a log line when unconfigured).

## Definition of done (every module)

1. Write the **positive and negative scenarios first** (a short list), then implement.
2. Domain rules live in the entity (unit-testable without Spring); the service orchestrates; the controller only maps HTTP and roles.
3. Tests, written by you, in the same change:
   - **Unit tests** for each domain rule, positive and negative.
   - **One end-to-end API test** (`@SpringBootTest` + `TestRestTemplate`, own in-memory H2 name) that walks the module in order: happy path, validation failures (400), duplicates (409), rule violations (422), role matrix (403 per role), tenant isolation using a second registered hospital (404), anonymous (401).
   - Every negative test asserts the status **and** that state did not change.
4. `mvn test` fully green, including `ModularityTest`; frontend `type-check`, `lint`, `build` clean.
5. Verify in the running app, not just in tests, and say plainly what you could not verify.
6. Small commits per concern; do not push unless asked.

## Modules still to build or finish

For each, the scenarios to cover (extend them, don't shrink them).

### Bed management — backend done, UI and integration left
- Done: wards, add beds, reduce beds (free beds only, highest number first), assign / release, maintenance, occupancy summary.
- Left: ward/bed UI with add/reduce controls; assign a bed from the admission flow and release it on discharge (event from `patients`, not a direct call); show bed on the patient profile; wire `BedSummaryResponse` into the live dashboard.
- Negative: reduce below occupied count; double-assign a bed or a patient; discharge with no bed; two nurses assigning the last bed at once (optimistic locking / unique index must give one winner).

### Billing and insurance
- Invoice per visit/admission from consultation fee, lab orders, dispensed medicines, bed-days; payments (partial, full, refund); insurer/TPA, claim lifecycle (DRAFT → SUBMITTED → APPROVED / REJECTED → SETTLED), billing codes.
- Positive: invoice totals and tax are exact (BigDecimal, no float); partial payments keep the invoice open; approved claim reduces patient due.
- Negative: pay more than due; edit a paid/finalised invoice; submit a claim twice; claim for a patient with no policy; negative or zero line items; cross-tenant invoice access; only ADMIN/billing staff may refund.

### Pharmacy and inventory (extend)
- Expiry alerts (event when stock expires within N days / already expired), batch tracking, dispense against a prescription decrementing stock.
- Negative: dispense expired batch; dispense more than stock; dispense a cancelled prescription; dispense the same prescription twice.

### Laboratory integration (extend)
- Structured results (value, unit, reference range, abnormal flag), attachments, result routed to the patient profile and ordering doctor.
- Negative: complete an order that isn't started; result outside plausible bounds; result for another patient's order; edit a released result.

### EHR (extend)
- One longitudinal timeline per patient: visits, diagnoses, prescriptions, lab results, admissions, vitals; allergies surfaced on prescribing.
- Negative: prescribing a drug matching a recorded allergy must warn/block; only treating staff can read; every read of a record is audited.

### Live dashboard
- Real-time tiles (bed occupancy, today's appointments by status, queue length, low stock, pending labs) via SSE or short polling; role-aware content.
- Negative: stale/failed stream shows a clear "not live" state; a role never receives tiles it can't open; tenant isolation on the stream.

### Template management
- Reusable templates for prescriptions, discharge summaries, lab report layouts, notification messages; variables (`{{patient.name}}`), versioning, preview.
- Negative: unknown variable rejected at save; deleting a template in use is blocked (or archives it); a template can't leak another hospital's data.

### Staff management and staff summary
- Staff directory (role, department, shift, status), onboarding/offboarding, and a **staff summary** page reached by clicking a staff member: profile, assigned doctors/wards, today's duty, activity.
- Negative: deactivated staff can't sign in; can't remove the last ADMIN; can't change own role; duplicate email; role changes audited.

## Output expected each time

1. The scenario list (positive / negative).
2. The implementation, in small commits.
3. The tests you wrote and their result (counts, not just "passed").
4. What you verified live, what you didn't, and what is left.
