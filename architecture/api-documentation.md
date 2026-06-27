# API Documentation

Base URL: application root (no context path configured). Default port: 8080 (8082 commented in config).

**Authentication:** Most endpoints require `Authorization: Bearer <JWT>`. JWT claim `userId` used where noted. Login endpoint is public.

**Response wrapper:** Most endpoints return `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "...",
  "data": { },
  "statusCode": 200
}
```

---

## Authentication

### POST `/api/login`

| Field | Value |
|-------|-------|
| **Auth** | None (public) |
| **Request** | `LoginRequest` — `{ "username": string, "password": string }` |
| **Response** | `ApiResponse<UserDto>` |
| **Service** | `AuthService.authenticate(LoginRequest)` |
| **Flow** | Lookup user by email → plain-text password compare → return user DTO |

---

## Users — `/api/users`

### POST `/api/users`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `UserDto` |
| **Response** | `ApiResponse<UserDto>` (201) |
| **Service** | `UserService.createUser(userDto, loggedInUserId)` |
| **Flow** | Resolve caller from JWT `userId` → check local DB → check/create Azure user → assign roles/blueprint/manager → save locally |

### GET `/api/users`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<UserDto>>` |
| **Service** | `UserService.getAllUsers()` |

### GET `/api/users/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<UserDto>` (includes subordinates + applications) |
| **Service** | `UserService.getUserById(id)` |

### PUT `/api/users/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `UserDto` |
| **Response** | `ApiResponse<UserDto>` |
| **Service** | `UserService.updateUser(id, userDto)` |

### POST `/api/users/{id}/reset-password`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `ResetPasswordRequest` — `{ "oldPassword"?, "newPassword" }` |
| **Response** | `ApiResponse<String>` |
| **Service** | `UserService.resetPassword(id, request, jwt)` |
| **Flow** | Admin can reset any user; non-admin can only change own password with old password verification |

### GET `/api/users/search`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Query** | `username` (required), `page` (default 0), `size` (default 10) |
| **Response** | `ApiResponse<Page<UserDto>>` |
| **Service** | `UserService.searchUsersByUsername(username, page, size)` |

### GET `/api/users/departments/{departmentId}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<UserDto>>` |
| **Service** | `UserService.getUsersByDepartment(departmentId)` |

### GET `/api/users/managers`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<UserDto>>` |
| **Service** | `UserService.getAllManagers()` |

### POST `/api/users/sync`

| Field | Value |
|-------|-------|
| **Auth** | JWT required (but not enforced in SecurityConfig — falls under `/api/users/**`) |
| **Response** | `ApiResponse<String>` — `"Sync completed"` |
| **Service** | `UserService.syncUsersFromAzure()` |
| **Flow** | Fetch Azure users → deactivate missing → insert/update → sync Azure groups as roles |

---

## Roles — `/api/roles`

### POST `/api/roles`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `RoleDto` — `{ "name", "description" }` |
| **Response** | `ApiResponse<RoleDto>` |
| **Service** | `RoleService.createRole(roleDto)` |

### GET `/api/roles`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<RoleDto>>` |
| **Service** | `RoleService.getAllRoles()` |

### GET `/api/roles/search`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Query** | `name`, `page` (default 0), `size` (default 10) |
| **Response** | `ApiResponse<List<RoleDto>>` |
| **Service** | `RoleService.searchRolesByName(name, page, size)` |

### GET `/api/roles/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<RoleDto>` |
| **Service** | `RoleService.getRoleById(id)` |

### DELETE `/api/roles/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<Void>` |
| **Service** | `RoleService.deleteRole(id)` |

---

## Applications — `/api/applications`

### GET `/api/applications`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Query** | `page` (default 0), `size` (default 10) |
| **Response** | `ApiResponse<Page<ApplicationDto>>` |
| **Service** | `ApplicationService.getAllApplications(page, size)` |

### GET `/api/applications/search`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Query** | `name` (required), `page`, `size` |
| **Response** | `ApiResponse<Page<ApplicationDto>>` |
| **Service** | `ApplicationService.searchApplicationsByName(name, page, size)` |

### GET `/api/applications/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<ApplicationDto>` |
| **Service** | `ApplicationService.getApplicationById(id)` |

### GET `/api/applications/users/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<UserApplicationDto>>` |
| **Service** | `ApplicationService.getUserApplications(id)` |

---

## Blueprints — `/api/blueprints`

### GET `/api/blueprints`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<BlueprintResponse>>` |
| **Service** | `BluePrintService.getAllBlueprints()` |

### GET `/api/blueprints/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<BlueprintResponse>` |
| **Service** | `BluePrintService.getBlueprintById(id)` |

### POST `/api/blueprints`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `BlueprintRequest` — `{ "name", "jobTitleIds": [], "applications": [{ "applicationId", "roles": [] }] }` |
| **Response** | `ApiResponse<BlueprintResponse>` |
| **Service** | `BluePrintService.createBlueprint(request)` |

### PUT `/api/blueprints/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `BlueprintRequest` |
| **Response** | `ApiResponse<BlueprintResponse>` |
| **Service** | `BluePrintService.updateBlueprint(id, request)` |

### DELETE `/api/blueprints/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<Void>` |
| **Service** | `BluePrintService.deleteBlueprint(id)` |

---

## Companies — `/api/companies`

### POST `/api/companies`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `CompanyRequestDto` — company info + nested `ContactDto` + `approverId` |
| **Response** | Raw `"Created"` string (not `ApiResponse`) |
| **Service** | `CompanyService.createCompany(dto, jwt.userId)` |
| **Flow** | Create company → create primary contact via UserService → link approver |

### GET `/api/companies`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `List<CompanyResponseDto>` (raw, no wrapper) |
| **Service** | `CompanyService.getAllCompanies()` |

### GET `/api/companies/my`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `List<CompanyResponseDto>` |
| **Service** | `CompanyService.getMyCompanies(jwt.userId)` |

### PUT `/api/companies/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `CompanyRequestDto` |
| **Response** | Raw `"Updated"` |
| **Service** | `CompanyService.updateCompany(id, dto)` |

### DELETE `/api/companies/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | Raw `"Deleted"` |
| **Service** | `CompanyService.deleteCompany(id)` |

---

## Departments — `/api/departments`

### GET `/api/departments`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<DepartmentResponseDTO>>` |
| **Service** | `DepartmentService.getAllDepartments()` |

---

## Job Titles — `/api/job-titles`

### GET `/api/job-titles`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<JobTitleResponse>>` |
| **Service** | `JobTitleService.getAllJobTitles()` |

### GET `/api/job-titles/{id}`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<JobTitleResponse>` |
| **Service** | `JobTitleService.getById(id)` |

---

## Delegates — `/api/delegates`

### POST `/api/delegates/request`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `DelegateRequestDTO` — `{ "targetDepartmentId", "comments" }` |
| **Response** | `ApiResponse<Void>` — `"Request submitted"` |
| **Service** | `DelegateService.createRequest(jwt.userId, dto)` |

### GET `/api/delegates/requests`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Query** | `departmentId` (Long) |
| **Response** | `ApiResponse<List<DelegateRequestResponseDTO>>` |
| **Service** | `DelegateService.getPendingRequests(departmentId)` |

### POST `/api/delegates/{id}/action`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `DelegateActionDTO` — `{ "status": "APPROVED"|"REJECTED", "comments" }` |
| **Response** | `ApiResponse<Void>` — `"Action completed"` |
| **Service** | `DelegateService.actOnRequest(id, jwt.userId, dto)` |
| **Flow** | Update request status → if APPROVED, create `UserDepartmentAccess` |

### GET `/api/delegates/users`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<DelegatedUserDto>>` |
| **Service** | `DelegateService.getDelegatedUsers(jwt.userId)` |

### GET `/api/delegates/my-requests`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `ApiResponse<List<DelegateRequestResponseDTO>>` |
| **Service** | `DelegateService.getMyRequests(jwt.userId)` |

### POST `/api/delegates/revoke`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `RevokeRequest` — `{ "requesterId", "targetDepartmentId", "comments" }` |
| **Response** | Raw `"Delegation revoked successfully"` |
| **Service** | `DelegateService.revokeDelegate(...)` |

---

## Settings — `/api/settings`

### GET `/api/settings`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Response** | `Map<String, Object>` (typed values for BOOLEAN settings) |
| **Service** | `SettingService.getAllSettings()` |

### POST `/api/settings`

| Field | Value |
|-------|-------|
| **Auth** | JWT required |
| **Request** | `Map<String, String>` |
| **Response** | Raw `"Settings saved"` |
| **Service** | `SettingService.saveAll(settings)` |

---

## Azure — `/api/azure`

### GET `/api/azure`

| Field | Value |
|-------|-------|
| **Auth** | **None** (permitAll — not in protected list) |
| **Response** | `List<User>` (Microsoft Graph model — not DTO) |
| **Service** | `AzureADService.getAllUsers()` |

### POST `/api/azure/user/create`

| Field | Value |
|-------|-------|
| **Auth** | **None** |
| **Query** | `displayName`, `mail` |
| **Response** | `User` (Graph model) |
| **Service** | `AzureADService.createUser(displayName, mail)` |

### DELETE `/api/azure/{userId}`

| Field | Value |
|-------|-------|
| **Auth** | **None** |
| **Response** | Raw `"User deleted"` |
| **Service** | `AzureADService.deleteUser(userId)` |

---

## Actuator (Public)

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/actuator/health` | Health check (details always shown) |
| GET | `/actuator/info` | Application info |

---

## Endpoint Summary

| Controller | Endpoints | Authenticated |
|------------|-----------|---------------|
| LoginController | 1 | 0 |
| UserController | 9 | 9 |
| RoleController | 5 | 5 |
| ApplicationController | 4 | 4 |
| BlueprintController | 5 | 5 |
| CompanyController | 5 | 5 |
| DepartmentController | 1 | 1 |
| JobTitleController | 2 | 2 |
| DelegateController | 6 | 6 |
| SettingController | 2 | 2 |
| AzureController | 3 | 0 |
| **Total** | **43** | **39** |

---

## API Design Observations

1. **No API versioning** (`/api/v1/...`)
2. **No OpenAPI/Swagger** dependency despite security whitelist for `/swagger/**`
3. **Inconsistent response envelopes** — Company, Azure, Settings endpoints bypass `ApiResponse`
4. **Azure endpoints unauthenticated** — direct Graph API proxy without authorization
5. **No input validation** — `@Valid` / Bean Validation not used on DTOs
6. **No role-based endpoint authorization** — authentication only, no `@PreAuthorize`
7. **Dead DTOs** — approval workflow DTOs have no corresponding endpoints
