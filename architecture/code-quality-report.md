# Code Quality Report

## Overview

Analysis of the identity-framework codebase for code smells, architectural violations, and maintainability concerns. No production code was modified during this assessment.

---

## Code Smells

### 1. God Service — `UserServiceImpl`

| Metric | Value |
|--------|-------|
| Lines | ~605 |
| Responsibilities | User CRUD, Azure sync, role assignment, blueprint assignment, manager hierarchy, password reset, company manager linking |
| Repositories | 5 |
| External integrations | Azure AD |

**Violates:** Single Responsibility Principle

**Evidence:** Single class handles identity provisioning, Azure synchronization, organizational hierarchy, and access template assignment.

---

### 2. Inconsistent Password Handling

| Location | Behavior |
|----------|----------|
| `AuthServiceImpl.authenticate(LoginRequest)` | Plain-text comparison |
| `UserServiceImpl.createUser` (Azure sync path) | Sets password `"Test123"` unencoded |
| `UserServiceImpl.createUser` (new user path) | BCrypt encode `"Test@123"` |
| `UserServiceImpl.resetPassword` | BCrypt encode/decode |

**Impact:** Security vulnerability and unpredictable authentication behavior.

---

### 3. Duplicated Logic

| Duplication | Locations |
|-------------|-----------|
| User creation from Azure data | `UserServiceImpl.createUser` (existing Azure user branch) vs `syncUsersFromAzure` (new user branch) |
| Blueprint assignment | Repeated in `createUser` and `updateUser` |
| Company manager assignment | Repeated in both branches of `createUser` |
| Name parsing (`getFirstName`/`getLastName`) | Only in UserServiceImpl but same logic needed elsewhere |
| Role assignment loop | Duplicated in createUser branches |
| UserApplicationDto mapping | `UserMapper.toUserApplicationDto` vs inline mapping in `ApplicationService.getUserApplications` |

---

### 4. Magic Strings / Hardcoded Values

| Value | Location | Purpose |
|-------|----------|---------|
| `"user"` | UserServiceImpl | Default role name |
| `"manager"` | UserRepository | Manager role filter |
| `"administration"`, `"super_admin"` | UserServiceImpl | Admin check |
| `"Test123"`, `"Test@123"` | UserServiceImpl | Default passwords |
| `"StrongPassword123!"` | AzureADService | Azure default password |
| `"@NETORGFT16179011.onmicrosoft.com"` | AzureADService | Hardcoded tenant domain |
| `"Slack"`, `"Jira"` | UserMapper | Essential app flag |
| `"UNKNOWN"` | JobTitleServiceImpl | Filter exclusion |

No constants class or enum for these domain values.

---

### 5. Dead Code & Unused Artifacts

| Artifact | Status |
|----------|--------|
| `CompanyApprovalRequest` entity | No repository, service, or controller |
| `CompanyContact` entity | Relationship commented out on Company |
| `CompanyContactRepository` | No service usage |
| `ApproveApprovalRequestDto`, `RejectApprovalRequestDto`, `CompanyApprovalResponseDto` | No endpoints |
| `LoginResponse` DTO | Unused (login returns UserDto) |
| `PaginatedResponse` DTO | Unused |
| `ApplicationRoleRepository` in BluePrintServiceImpl | Injected, never called |
| `AuthServiceImpl` ROPC flow | ~30 lines commented out |
| `RoleRepository.findByIdWithActiveUsers` | Commented out (wouldn't compile) |
| `AzureDataInitializer` sync call | Commented out |
| `UserServiceImpl.updateUser` Azure update | Commented out |
| `TestRunner` | Empty dev stub registered as `@Configuration` |
| `jira.*` config properties | No code references |
| `ExternalSource` enum | Fields use String instead |
| Duplicate `oauth2-resource-server` dependency in pom.xml | Declared twice |

---

### 6. Exception Handling Anti-Patterns

| Issue | Detail |
|-------|--------|
| Catch-all `RuntimeException` → 400 | `GlobalExceptionHandler` maps ALL runtime exceptions to BAD_REQUEST |
| Swallowed exceptions | `AuthServiceImpl` catches `Exception` and returns 500 with message |
| Generic wrapping | `UserServiceImpl` catches `Exception` and wraps in `ApiException` |
| Inconsistent exception types | Mix of `ApiException` (checked), `RuntimeException`, `BadRequestException`, `ResourceNotFoundException`, `ResponseStatusException` |
| No validation exceptions | No `MethodArgumentNotValidException` handler |

---

### 7. Performance Concerns

| Issue | Location | Impact |
|-------|----------|--------|
| `findAll()` for subordinate map | `UserServiceImpl.searchUsersByUsername` | O(n) load of all users on every search |
| `findAll()` in sync | `UserServiceImpl.syncUsersFromAzure` | Full table scan every 10 min |
| Azure calls inside `@Transactional` | `UserServiceImpl` | DB connection held during network I/O |
| No pagination on Azure Graph | `AzureADService.getAllUsers/getAllGroups` | Only first page fetched; breaks at scale |
| N+1 potential | `UserServiceImpl.getAllUsers` | Subordinates fetched per user |
| `@EntityGraph` only on manager query | UserRepository | Other role-based queries may N+1 |

---

### 8. Tight Coupling

| Coupling | Severity |
|----------|----------|
| `UserServiceImpl` → `AzureADService` (direct Graph model usage) | High |
| `CompanyService` → `UserService` (concrete, not interface boundary) | Medium |
| Controllers → concrete service classes (CompanyService, ApplicationService, AzureADService) | Medium |
| `UserMapper` → hardcoded application names | Low |
| Security route list manually maintained in SecurityConfig | Medium |

---

### 9. Inconsistent Patterns

| Pattern | Inconsistency |
|---------|---------------|
| Service interfaces | 7 with interfaces, 4 without |
| Mappers | Mix of static utility (`UserMapper`) and `@Component` beans (`BlueprintMapper`) |
| Response wrapping | `ApiResponse<T>` vs raw types vs plain strings |
| Lombok usage | `@Data` on entities (equals/hashCode issues with relationships) vs explicit getters on Blueprint |
| Transaction annotations | Class-level, method-level, readOnly — applied inconsistently |
| Logging | `@Slf4j` on some services, none on others |

---

### 10. Lombok `@Data` on JPA Entities

Entities like `User`, `Company`, `DelegateRequest` use `@Data` which generates `equals`/`hashCode` including all fields and relationships — can cause issues with:
- Hibernate proxy initialization
- Collection management in `@OneToMany`/`@ManyToMany`
- Infinite recursion on bidirectional relationships

`Blueprint` correctly uses `@Getter/@Setter/@EqualsAndHashCode(onlyExplicitlyIncluded=true)`.

---

## Cyclic Dependencies

**No compile-time cyclic dependencies detected.**

Potential runtime coupling:
- `CompanyService` → `UserService` (if UserService ever needs company context, cycle would form)

---

## Large Classes

| Class | Lines (approx) | Concern |
|-------|----------------|---------|
| `UserServiceImpl` | ~605 | God service |
| `SecurityConfig` | ~110 | Acceptable |
| `BluePrintServiceImpl` | ~263 | Acceptable |
| `DelegateServiceImpl` | ~167 | Acceptable |

---

## SRP Violations

| Class | Responsibilities Count | Should Split Into |
|-------|----------------------|-------------------|
| `UserServiceImpl` | 6+ | UserCrudService, AzureSyncService, UserPasswordService |
| `CompanyService` | 2 | CompanyService + delegate user creation to dedicated orchestrator |
| `GlobalExceptionHandler` | Catches RuntimeException too broadly | Specific handlers per exception type |

---

## Security Code Quality Issues

| Issue | Severity |
|-------|----------|
| Secrets in `application.yaml` | Critical |
| Plaintext passwords | Critical |
| Unauthenticated `/api/azure/**` | Critical |
| No input validation | High |
| OData filter string concatenation | Medium |
| SSN stored unencrypted | High |

---

## Test Coverage

| Category | Count | Notes |
|----------|-------|-------|
| Unit tests | 0 | None |
| Integration tests | 0 | None |
| Context load test | 1 | `IdentityFrameworkApplicationTests.contextLoads()` |
| Service tests | 0 | |
| Controller tests | 0 | |
| Repository tests | 0 | |
| Security tests | 0 | `spring-security-test` dependency present but unused |

**Estimated coverage: ~0%** of business logic.

---

## Missing Tests (Priority)

1. `AuthServiceImpl` — password and JWT authentication paths
2. `UserServiceImpl.syncUsersFromAzure` — deactivation and insert logic
3. `DelegateServiceImpl` — full approval/revoke workflow
4. `BluePrintServiceImpl` — CRUD validation
5. `SecurityConfig` — route authorization rules
6. `GlobalExceptionHandler` — exception mapping
7. `AzureADService` — mock Graph client interactions

---

## Documentation Quality

| Document | Status |
|----------|--------|
| README.md | **Missing** |
| HELP.md | Spring Boot generated stub only |
| Architecture docs | Created in `/architecture` (this analysis) |
| ADRs | None |
| API docs (OpenAPI) | Not configured |
| Inline Javadoc | Minimal |

---

## Build Quality

| Aspect | Status |
|--------|--------|
| Duplicate dependency | `oauth2-resource-server` declared twice |
| Annotation processors | Lombok only (no MapStruct) |
| Compiler warnings | Not assessed (no build run) |
| Static analysis | No Checkstyle/SpotBugs/PMD configured |
| CI/CD | No pipeline files found in repo |

---

## Summary Scorecard

| Dimension | Rating | Key Issue |
|-----------|--------|-----------|
| Maintainability | Poor | God service, duplication, inconsistency |
| Security | Critical | Plaintext passwords, exposed secrets, open Azure API |
| Testability | Poor | Zero meaningful tests |
| Scalability | Poor | Full table scans, no pagination, sync in transaction |
| Consistency | Poor | Mixed patterns across layers |
| Documentation | Poor | No README, no API docs |
