# Dependency Flow

## Request Flow — Authenticated API Call

```mermaid
sequenceDiagram
    participant C as Client
    participant SF as SecurityFilterChain
    participant JD as NimbusJwtDecoder
    participant CT as Controller
    participant SV as Service
    participant RP as Repository
    participant DB as PostgreSQL
    participant AZ as Azure Graph

    C->>SF: GET /api/users/{id} + Bearer JWT
    SF->>JD: Decode & validate JWT
    JD-->>SF: Jwt principal
    SF->>CT: Authorized request
    CT->>SV: UserService.getUserById(id)
    SV->>RP: UserRepository.findById(id)
    RP->>DB: SELECT FROM users
    DB-->>RP: User entity
    SV->>RP: UserApplicationRepository.findByUserIdAndActiveTrue(id)
    RP->>DB: SELECT FROM user_applications
    DB-->>RP: UserApplication list
    SV->>RP: UserRepository.findByManagerId(id)
    RP->>DB: SELECT subordinates
    DB-->>RP: User list
    SV-->>CT: UserDto
    CT-->>C: ApiResponse<UserDto>
```

---

## Request Flow — User Creation (with Azure)

```mermaid
flowchart TD
    A[POST /api/users] --> B[UserController]
    B --> C[UserService.createUser]
    C --> D{Email exists locally?}
    D -->|Yes| E[Throw: already exists]
    D -->|No| F[AzureADService.getUserByEmail]
    F --> G{Exists in Azure?}
    G -->|Yes| H[Build local user from Azure data]
    G -->|No| I[AzureADService.createUser]
    I --> H
    H --> J[Assign roles from RoleRepository]
    J --> K[Assign blueprint from BlueprintRepository]
    K --> L[Set manager from CompanyRepository or logged-in user]
    L --> M[UserRepository.save]
    M --> N[Return UserDto via UserMapper]
```

---

## Request Flow — Azure Sync (Scheduled)

```mermaid
flowchart TD
    A[AzureUserSyncScheduler<br/>every 10 min] --> B[UserService.syncUsersFromAzure]
    B --> C[AzureADService.getAllUsers]
    C --> D[Mark local users not in Azure as inactive]
    D --> E[For each Azure user]
    E --> F{Exists locally?}
    F -->|No| G[Insert new User with default role]
    F -->|Yes| H[AzureUserUpdater.updateUserFromAzure]
    H --> I{Changed?}
    I -->|Yes| J[UserRepository.save]
    G --> E
    J --> E
    E --> K[syncRolesFromAzure]
    K --> L[AzureADService.getAllGroups]
    L --> M[Create missing Role entries]
```

---

## Request Flow — Delegation Approval

```mermaid
flowchart TD
    A[POST /api/delegates/request] --> B[DelegateService.createRequest]
    B --> C{Pending request exists?}
    C -->|Yes| D[Throw: already pending]
    C -->|No| E[Save DelegateRequest PENDING]

    F[POST /api/delegates/{id}/action] --> G[DelegateService.actOnRequest]
    G --> H[Update request status]
    H --> I{Status = APPROVED?}
    I -->|Yes| J[Create UserDepartmentAccess]
    I -->|No| K[Done]

    L[POST /api/delegates/revoke] --> M[DelegateService.revokeDelegate]
    M --> N[Set status REVOKED]
    N --> O[Delete UserDepartmentAccess]
```

---

## Request Flow — Login (Password)

```mermaid
flowchart TD
    A[POST /api/login] --> B[LoginController]
    B --> C[AuthService.authenticate]
    C --> D[UserRepository.findByEmail]
    D --> E{User found & password matches?}
    E -->|No| F[Return 401 ApiResponse]
    E -->|Yes| G[UserMapper.toDto]
    G --> H[Return 200 ApiResponse]
```

---

## Layer Dependency Graph

```mermaid
graph TD
    subgraph Presentation
        LC[LoginController]
        UC[UserController]
        RC[RoleController]
        AC[ApplicationController]
        BC[BlueprintController]
        CC[CompanyController]
        DC[DepartmentController]
        JC[JobTitleController]
        DEC[DelegateController]
        SC[SettingController]
        AZC[AzureController]
    end

    subgraph Services
        AUTH[AuthServiceImpl]
        USER[UserServiceImpl]
        ROLE[RoleServiceImpl]
        APP[ApplicationService]
        BLUE[BluePrintServiceImpl]
        COMP[CompanyService]
        DEPT[DepartmentServiceImpl]
        JOB[JobTitleServiceImpl]
        DELEG[DelegateServiceImpl]
        SET[SettingService]
        AZS[AzureADService]
    end

    subgraph Infrastructure
        UR[UserRepository]
        RR[RoleRepository]
        AR[ApplicationRepository]
        UAR[UserApplicationRepository]
        BR[BlueprintRepository]
        CR[CompanyRepository]
        DR[DepartmentRepository]
        JR[JobTitleRepository]
        DRR[DelegateRequestRepository]
        UDAR[UserDepartmentAccessRepository]
        SR[SettingRepository]
    end

    subgraph External
        PG[(PostgreSQL)]
        GRAPH[Microsoft Graph]
    end

    LC --> AUTH
    UC --> USER
    RC --> ROLE
    AC --> APP
    BC --> BLUE
    CC --> COMP
    DC --> DEPT
    JC --> JOB
    DEC --> DELEG
    SC --> SET
    AZC --> AZS

    AUTH --> UR
    USER --> UR & RR & UAR & BR & CR & AZS
    ROLE --> RR
    APP --> AR & UAR
    BLUE --> BR & JR & AR
    COMP --> CR & UR & USER
    DEPT --> DR
    JOB --> JR
    DELEG --> DRR & UDAR & UR & DR
    SET --> SR
    AZS --> GRAPH

    UR & RR & AR & UAR & BR & CR & DR & JR & DRR & UDAR & SR --> PG
```

---

## Controller → Service → Repository Quick Reference

| Controller | Service | Repositories |
|------------|---------|-------------|
| LoginController | AuthServiceImpl | UserRepository |
| UserController | UserServiceImpl | User, Role, UserApplication, Blueprint, Company |
| RoleController | RoleServiceImpl | RoleRepository |
| ApplicationController | ApplicationService | Application, UserApplication |
| BlueprintController | BluePrintServiceImpl | Blueprint, JobTitle, Application |
| CompanyController | CompanyService | Company, User (+ UserService) |
| DepartmentController | DepartmentServiceImpl | DepartmentRepository |
| JobTitleController | JobTitleServiceImpl | JobTitleRepository |
| DelegateController | DelegateServiceImpl | DelegateRequest, UserDepartmentAccess, User, Department |
| SettingController | SettingService | SettingRepository |
| AzureController | AzureADService | — (Graph API only) |

---

## Cross-Cutting Concerns Flow

```mermaid
flowchart LR
    REQ[HTTP Request] --> CORS[CORS Filter]
    CORS --> SEC[Security Filter Chain]
    SEC --> JWT{JWT Valid?}
    JWT -->|No| REJ[401 Unauthorized]
    JWT -->|Yes| CTRL[Controller]
    CTRL --> SVC[Service]
    SVC -->|Exception| GEH[GlobalExceptionHandler]
    GEH --> RESP[ApiResponse Error]
    SVC -->|Success| MAP[Mapper]
    MAP --> RESP2[ApiResponse Success]
```

---

## Startup Flow

```mermaid
flowchart TD
    A[SpringApplication.run] --> B[Auto-config + Component Scan]
    B --> C[SecurityConfig beans registered]
    B --> D[AzureADService init → Graph client]
    B --> E[AzureDataInitializer.run]
    E --> F[Sync commented out — log only]
    B --> G[AzureUserSyncScheduler registered]
    G --> H{EnableScheduling?}
    H -->|No| I[Scheduler inactive]
    H -->|Yes| J[Sync every 10 min]
    B --> K[Hibernate ddl-auto update]
    K --> L[Schema sync with PostgreSQL]
```

---

## Data Flow — Blueprint to User Access (Conceptual)

```mermaid
flowchart LR
    JT[Job Title] --> BP[Blueprint]
    BP --> BAR[BlueprintApplicationRole]
    BAR --> APP[Application]
    BAR --> RN[Role Name string]
    USER[User] -->|blueprint_id| BP
    USER --> UA[UserApplication]
    UA --> APP

    style RN fill:#f96,stroke:#333
```

**Gap:** No automated flow from blueprint assignment to `user_applications` provisioning visible in service layer. Blueprint defines template; user applications appear to be managed separately.
