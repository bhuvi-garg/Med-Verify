# Architecture — Med-Verify

Status: **Approved**

This is the architecture derived from [Requirements/](../Requirements/README.md), covering how the system is built to support two frontends (Android now, Web UI in Phase 2) sharing one backend.

## Documents

0. [ARCH-00 — Overview](ARCH-00-overview.md) — the big picture: clients, backend, data layer, and why this shape was chosen.
1. [ARCH-01 — Components](ARCH-01-components.md) — what each piece (Android app, Web UI, backend layers) is responsible for.
2. [ARCH-02 — Authentication](ARCH-02-authentication.md) — account model, caretaker-assisted onboarding, session handling.
3. [ARCH-03 — Data Model](ARCH-03-data-model.md) — core entities and how they relate.
4. [ARCH-04 — Deployment](ARCH-04-deployment.md) — self-hosted, containerized deployment layout.
5. [ARCH-05 — Key Flows](ARCH-05-flows.md) — scan flow and reminder/escalation flow, sequence diagrams.
6. [ARCH-06 — Scan Combination Behavior](ARCH-06-scan-combination-behavior.md) — what happens for every combination of medicine/prescription/bill scans, and the corner cases that fall out of getting this wrong.

## Key decisions

- **One backend, two clients**: all REQ-01–REQ-15 business logic lives once, in a Python backend. The Android app and the Phase 2 Web UI are both thin clients against the same API — no duplicated logic, no rework when the Web UI is added.
- **Python (FastAPI) backend** — chosen partly for easy integration with AI models in Phase 2 (REQ-09, Sarvam AI).
- **PostgreSQL**, not the SQLite mentioned in early requirements drafts — a shared multi-client backend needs real concurrent access, which SQLite doesn't comfortably provide.
- **Account-based auth**, set up by the caretaker during onboarding (REQ-15), not a bare device ID — this avoids a painful migration when Phase 2 needs caretaker accounts linked to the same elderly user's data.
- **Separate caretaker accounts, many-to-many linkable** (REQ-16, Phase 2): a caretaker has their own login, distinct from any elderly user's, and can be linked to multiple elderly users — one dashboard login covers everyone they look after.
- **Self-hosted, containerized deployment** (Docker Compose), so hosting location can change without an architecture change.
