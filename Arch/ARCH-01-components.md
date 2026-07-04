# ARCH-01 — Components

Status: Approved

## Android app (Phase 1)

- Elderly-facing, always-simplified UI ([REQ-11](../Requirements/REQ-11-simplified-ui-mode.md)) — the app has exactly one UI mode, no denser alternative ever appears here.
- Responsibilities: camera capture for scans, rendering results, TTS playback in the user's local language ([REQ-03](../Requirements/REQ-03-label-reading.md)), showing in-app reminders ([REQ-06](../Requirements/REQ-06-dosage-reminder.md)), local caching of recent data for offline resilience.
- Does **not** contain REQ-01–REQ-15 business logic — it calls the backend API and renders what comes back.
- Caretaker-assisted onboarding ([REQ-15](../Requirements/REQ-15-assisted-onboarding.md)) happens here: account login, initial prescription/bill scans, escalation contact setup.
- Stays signed in persistently after onboarding (see [ARCH-02](ARCH-02-authentication.md)) — the elderly user never sees a login screen day-to-day.

## Web UI (Phase 2)

- Caretaker-facing dashboard ([REQ-10](../Requirements/REQ-10-caretaker-web-dashboard.md)): denser UI, ongoing medicine/contact management, adherence and escalation history.
- Login is with the caretaker's own account, not the elderly user's credentials. The landing screen is a list of every elderly user linked to that caretaker ([REQ-16](../Requirements/REQ-16-caretaker-multi-patient-linking.md)), from which one is selected to view/manage.
- Talks to the same backend API as the Android app — no separate backend, no duplicated logic.
- This is where caretaker-facing complexity is intentionally concentrated, keeping the Android app minimal (per REQ-15's decision that caretakers shouldn't routinely return to the app to manage medicines).

## Backend (Python, FastAPI)

### API layer
- REST endpoints grouped by requirement domain: `/scan` (classification, REQ-01), `/medicines`, `/prescriptions`, `/bills`, `/reminders`, `/escalation-contacts`, and (Phase 2) `/chat`.
- Thin — validates requests, calls the service layer, shapes responses. No business logic here.

### Service layer
- Where REQ-01–REQ-15 actually live: scan classification, chemical-equivalence matching (REQ-02), dosage source fallback chain (REQ-04), prescription/bill extraction fallback chains (REQ-05/REQ-07), refill math (REQ-08), interaction/expiry checks (REQ-12/REQ-14), escalation rules (REQ-13).
- Framed as independent services per domain so they can be tested and reasoned about in isolation, but all reachable from the API layer.

### Scheduler
- Runs reminder and escalation checks independent of whether a client is connected (APScheduler or Celery+Redis — exact choice deferred).
- Reads due reminders/escalations from PostgreSQL, triggers delivery (in-app push to Android for reminders, SMS for escalation per REQ-13).

### Integration adapters
- Thin wrappers around external services: OCR, translation, TTS (REQ-03), and (Phase 2) the AI chat model (REQ-09, Sarvam AI prioritized).
- Exist specifically so the service layer never hardcodes a vendor — swapping an OCR provider, for instance, only touches its adapter.

## Data layer

- **PostgreSQL** — see [ARCH-03](ARCH-03-data-model.md) for entities. Holds both static reference data (medicine/chemical/interaction data) and per-user state (scans, reminders, adherence, escalation contacts).
