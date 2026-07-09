# ADR 004: Flyway Schema Management

## Status
Accepted

## Context
Schema evolved organically with `ddl-auto` and no migration history, risking drift between environments.

## Decision
- **Production:** `spring.jpa.hibernate.ddl-auto=validate` with Flyway migrations enabled
- **Local / test:** Flyway disabled; H2 or existing Postgres used for development
- Baseline existing databases with `baseline-on-migrate`

## Consequences
- Reproducible schema for new environments via `db/migration`
- Existing DBs require one-time baseline alignment
- Entity changes must ship with corresponding migration scripts in prod
