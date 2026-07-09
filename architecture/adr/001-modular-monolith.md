# ADR 001: Modular Monolith

## Status
Accepted

## Context
The identity platform coordinates users, Azure Entra ID, blueprints, companies, and entitlements. Early analysis considered splitting sync workers and API surfaces into separate deployables.

## Decision
Remain a **modular monolith** with clear package boundaries (`controller`, `services`, `repositories`, `domain`, `access`, `config`, `security`). Extract workers only when operational metrics justify independent scaling.

## Consequences
- Simpler deployment and transactions across user provisioning flows
- Requires discipline (ArchUnit, service splits) to avoid a “big ball of mud”
- Future extraction of Azure sync remains possible behind `AzureSyncService`
