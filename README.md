# Identity Framework

Central identity and access management (IAM) platform for nDash. Manages users, roles, application entitlements, access blueprints, company onboarding, and department delegation with Microsoft Entra ID integration.

## Prerequisites

- Java 21
- Maven 3.9+
- Docker (optional, for local PostgreSQL)

## Quick start (local)

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Configure environment

```bash
cp .env.example .env
```

Edit `.env` with your values. For local development:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres?currentSchema=identity_framework
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
AZURE_TENANT_ID=<your-tenant-id>
AZURE_CLIENT_ID=<your-client-id>
AZURE_CLIENT_SECRET=<your-client-secret>
JWT_SECRET=<your-jwt-secret>
SPRING_PROFILES_ACTIVE=local
```

Export variables before running:

```bash
set -a && source .env && set +a
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

### 4. Verify

- Health: http://localhost:8080/actuator/health
- OpenAPI docs: http://localhost:8080/swagger-ui.html
- API spec: http://localhost:8080/api-docs

## Profiles

| Profile | Purpose |
|---------|---------|
| `local` | Development — Hibernate `ddl-auto: update`, debug logging |
| `prod` | Production — Hibernate `validate`, Flyway enabled |

## Production deployment

1. Set all environment variables from `.env.example` in your deployment platform (Coolify, etc.)
2. Activate the `prod` profile: `SPRING_PROFILES_ACTIVE=prod`
3. On first deploy against an **existing** Hibernate-managed database, Flyway will baseline automatically (`baseline-on-migrate`)
4. On a **fresh** database, Flyway runs `V1__initial_schema.sql`

**Important:** Rotate any credentials that were previously committed to source control.

## Build & test

```bash
./mvnw verify
```

## API authentication

Most endpoints require `Authorization: Bearer <JWT>`.

Public endpoints:
- `POST /api/login`
- `GET /actuator/health`
- `GET /actuator/info`
- OpenAPI/Swagger UI

## Project structure

```
src/main/java/com/ndash/identity_framework/
├── config/         Security, OpenAPI, schedulers
├── controller/     REST API
├── domain/         JPA entities
├── dto/            Request/response objects
├── exception/      Global error handling
├── mapper/         Entity ↔ DTO mapping
├── repositories/   Spring Data JPA
└── services/       Business logic
```

Architecture documentation: [`architecture/`](architecture/)

## Technology stack

- Spring Boot 3.5 / Java 21
- PostgreSQL + Flyway
- Spring Security (OAuth2 resource server)
- Microsoft Graph (Azure Entra ID)
- SpringDoc OpenAPI
