# Design — Backend

Backend design decisions more granular than [Arch/](../../Arch/README.md)'s system-level view — e.g. specific endpoint request validation, error-handling behavior, and service-layer logic details for each requirement's fallback chains (REQ-04's dosage source order, REQ-05/07's extraction fallback, REQ-08's course-completion rules, etc.).

- **[tech-stack.md](tech-stack.md)** — concrete tooling decisions (FastAPI, SQLAlchemy, Alembic, APScheduler, pytest), exact dependency versions, the project scaffold, and test fixtures — project-wide.
- **[db-schema.md](db-schema.md)** — typed database schema, currently scoped to the medicine-scan vertical slice.
- **[units/](units/README.md)** — the backend broken into individual units (one external-facing API unit, one feature unit per piece of behavior, one shared Utility unit), each with its own folder: responsibility, exact interface, algorithm, and test cases — detailed enough to code and test directly from.

Only the medicine-scan slice is designed so far — prescriptions, bills, reminders, escalation, and caretaker/override logic will get their own unit breakdowns later. Note: OCR and initial scan classification are **not** backend units — they run on-device in the Android app (see [Design/Android/units/](../Android/units/README.md)) and [Arch/ARCH-00](../../Arch/ARCH-00-overview.md#why-not-put-logic-on-device-and-the-one-exception) for why.
