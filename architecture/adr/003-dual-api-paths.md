# ADR 003: Dual API Paths for Versioning

## Status
Accepted

## Context
Existing consumers call `/api/*` endpoints. A versioned surface is needed without breaking integrations.

## Decision
Expose each REST controller on **two base paths** via `ApiPaths`:
- Versioned: `/api/v1/*`
- Legacy: `/api/*`

New clients should prefer `/api/v1`. Legacy paths will be deprecated after consumer migration.

## Consequences
- Zero-downtime migration for API consumers
- Slightly more routing configuration per controller
- Deprecation policy and sunset timeline must be communicated externally
