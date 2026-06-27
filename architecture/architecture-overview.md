# Architecture Overview

## Executive Summary

**identity-framework** is a monolithic Spring Boot 3.5 application (Java 21) that serves as nDash's central identity and access management (IAM) platform. It manages users, roles, departments, companies, application entitlements, and access blueprints, with Microsoft Entra ID (Azure AD) as the primary external identity provider.

The system is a **single deployable unit** — not a microservices architecture. All business logic lives in one Maven module with a classic layered structure.

---

## Application Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Client Applications                          │
│   (Vercel frontend, idf.ndashdigital.com, localhost:3000)     │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS / JWT Bearer
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Boot Application                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ Controllers  │→ │   Services   │→ │    Repositories      │  │
│  │  (REST API)  │  │  (Business)  │  │  (Spring Data JPA)   │  │
│  └──────────────┘  └──────┬───────┘  └──────────┬───────────┘  │
│                           │                      │              │
│  ┌──────────────┐  ┌──────▼───────┐              │              │
│  │   Security   │  │   Mappers    │              │              │
│  │  (JWT/OAuth) │  │  (Manual)    │              │              │
│  └──────────────┘  └──────────────┘              │              │
└──────────────────────────┼───────────────────────┼──────────────┘
                           │                       │
              ┌────────────▼──────────┐   ┌────────▼────────┐
              │  Microsoft Graph API  │   │   PostgreSQL    │
              │  (Azure Entra ID)     │   │ identity_schema │
              └───────────────────────┘   └─────────────────┘
```

---

## Technology Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.4 |
| Web | Spring Web MVC |
| Security | Spring Security + OAuth2 Resource Server |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL (prod), H2 (available) |
| Identity | Azure Entra ID via Microsoft Graph SDK |
| JWT | Nimbus JOSE JWT (HMAC-SHA256 local decoder) |
| Build | Maven |
| Utilities | Lombok |
| Observability | Spring Actuator (`/actuator/health`, `/actuator/info`) |

**Not present:** Redis, message queues, MapStruct, OpenAPI/Swagger (paths whitelisted but no dependency), caching, async processing, Flyway/Liquibase.

---

## Layered Architecture

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Presentation | `controller` | REST endpoints, request/response mapping, JWT principal injection |
| Application | `services` / `services.impl` | Business logic, orchestration, transactions |
| Domain | `domain` | JPA entities, enums, embeddables |
| Infrastructure | `repositories` | Data access via Spring Data JPA |
| Cross-cutting | `config` | Security, CORS, schedulers, startup hooks |
| Mapping | `mapper` | Entity ↔ DTO conversion (manual static methods) |
| DTO | `dto` | API request/response contracts |
| Exception | `exception` | Custom exceptions + `@ControllerAdvice` handler |
| Helper | `helper` | Azure sync diff logic |
| Utility | `util` | Shared helpers (e.g., SSN masking) |

---

## Modules and Packages

Single Maven module: `com.ndash:identity-framework:0.0.1-SNAPSHOT`

```
com.ndash.identity_framework
├── IdentityFrameworkApplication.java   # Entry point
├── config/          # Security, schedulers, initializers
├── controller/      # 11 REST controllers
├── domain/          # 16 entities + 3 enums + 1 embeddable
├── dto/             # 28 DTO classes
├── exception/       # 4 exception types + global handler
├── helper/          # AzureUserUpdater
├── mapper/          # 5 mappers (manual, not MapStruct)
├── repositories/    # 13 JPA repositories
├── services/        # 11 service classes + impl/
└── util/            # CommonUtil
```

---

## Microservices Assessment

**None.** This is a monolith. All domains (users, roles, companies, blueprints, delegation) share one database schema, one deployment, and one process.

Future decomposition candidates (by bounded context):
- Identity sync (Azure AD integration)
- Access provisioning (blueprints + user applications)
- Delegation / department access
- Company onboarding

---

## Shared Libraries

No internal shared library modules. All code is self-contained in this repository. External libraries are consumed via Maven dependencies only.

---

## Dependency Flow

```
Controller → Service (interface or concrete) → Repository → PostgreSQL
                    ↓
              AzureADService → Microsoft Graph API
                    ↓
              AzureUserUpdater (helper)
```

**Cross-service dependencies:**
- `CompanyService` → `UserService` (creates primary contact users)
- `AzureUserSyncScheduler` / `AzureDataInitializer` → `UserService.syncUsersFromAzure()`
- Controllers inject services directly (no facade layer)

---

## Request Lifecycle

1. HTTP request arrives at Spring MVC dispatcher
2. CORS preflight handled for allowed origins
3. `SecurityFilterChain` validates JWT (HMAC-SHA256) for protected `/api/**` routes
4. Controller extracts `@AuthenticationPrincipal Jwt` where needed
5. Service executes business logic (possibly within `@Transactional` boundary)
6. Repository persists/reads from PostgreSQL (`identity_framework` schema)
7. Mapper converts entities to DTOs
8. `ApiResponse<T>` wrapper returned (inconsistent — some endpoints bypass this)
9. `GlobalExceptionHandler` catches unhandled exceptions

---

## Deployment Profile

- **Database:** PostgreSQL at `coolify.ndashdigital.com:5432`, schema `identity_framework`
- **DDL:** Hibernate `ddl-auto: update` (schema managed by application startup)
- **Frontend origins:** Vercel app, idf.ndashdigital.com, localhost:3000
- **Azure tenant:** Configured via `application.yaml` properties

---

## Key Architectural Characteristics

| Characteristic | Status |
|----------------|--------|
| Stateless API | Yes (JWT, no server sessions) |
| Event-driven | No |
| CQRS | No |
| API versioning | No |
| Database migrations | No (Hibernate auto-DDL) |
| Multi-tenancy | No |
| Caching | No |
| Background jobs | Scheduled Azure sync (10 min) — `@EnableScheduling` missing |

---

## Entry Point

```java
@SpringBootApplication
public class IdentityFrameworkApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityFrameworkApplication.class, args);
    }
}
```

Component scanning covers `com.ndash.identity_framework` and all sub-packages automatically.
