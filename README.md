# MedFlow AI

Java 21 / Spring Boot modular-monolith foundation for IntelliJ IDEA.

## Start locally

1. Install JDK 21 and Docker Desktop.
2. Run `docker compose up -d`.
3. Open `pom.xml` in IntelliJ IDEA, select JDK 21, then run `MedflowApplication`.
4. Browse `http://localhost:8080/swagger-ui/index.html`.

The public Patient endpoint is `POST /api/v1/patients`. Database migrations are module-owned in `db/migration/<module>`; Flyway locations are explicitly configured in `application.yml`.

## Module contract

Modules may depend only on another module's public application API or published events. They must not import another module's `domain`, `repository`, or `infrastructure` packages. Use the Patient module as the reference implementation when adding feature modules.
