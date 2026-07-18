# Design — Interop

The frontend↔backend interop contract: the actual JSON request/response shapes exchanged over the API, for every endpoint listed in [Arch/ARCH-01](../../Arch/ARCH-01-components.md) (`/scan`, `/medicines`, `/prescriptions`, `/bills`, `/reminders`, `/escalation-contacts`, and later `/chat`).

This is the single source of truth both frontends (Android, WebUI) and the Backend build against — field names, types, enums, error shapes — so that mocked frontend data and the real backend implementation don't drift apart. It builds directly on the entities in [Arch/ARCH-03](../../Arch/ARCH-03-data-model.md).

Scope each contract document to what's actually needed next, rather than trying to fully specify every endpoint upfront.

- **[scan-endpoint.md](scan-endpoint.md)** — `POST /scan` for the medicine-scan slice: request shape, and every response variant (resolved, expired, pending, error).

Only `/scan` is specified so far, and only its medicine path — the rest of the endpoints listed above are designed as their slices come up.
