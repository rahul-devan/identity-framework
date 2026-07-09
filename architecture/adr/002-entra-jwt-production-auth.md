# ADR 002: Production Authentication via Entra JWT

## Status
Accepted

## Context
The application previously used a shared HMAC secret for all environments and exposed password login in production-like paths.

## Decision
- **Production (`prod` profile):** OAuth2 resource server with Entra ID issuer (`JwtDecoders.fromIssuerLocation`)
- **Local / test:** HMAC JWT via `JWT_SECRET`; password login endpoint gated with `@Profile("local")`
- **Authorization:** Method security with DB-backed role authorities loaded in `JwtSecurityConfig`

## Consequences
- Production clients must obtain tokens from Entra ID
- Local developers retain password login for fast iteration
- Role changes in the database affect authorization without redeploying static role lists
