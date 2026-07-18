# ARCH-01 — Components

Status: Approved

## Android app (Phase 1)

- Elderly-facing, always-simplified UI ([REQ-11](../Requirements/REQ-11-simplified-ui-mode.md)) — the app has exactly one UI mode, no denser alternative ever appears here.
- Responsibilities: camera capture for scans, **on-device OCR and initial scan classification** (REQ-01 — deciding medicine vs. prescription vs. bill; the one piece of REQ-0X logic that lives here, not the backend, since it's consumed only by this app — see [ARCH-00](ARCH-00-overview.md#why-not-put-logic-on-device-and-the-one-exception)), rendering results, TTS playback in the user's local language ([REQ-03](../Requirements/REQ-03-label-reading.md)), showing in-app reminders ([REQ-06](../Requirements/REQ-06-dosage-reminder.md)), local caching of recent data for offline resilience.
- **Every scan uploads the image to the backend**, regardless of whether on-device OCR/classification succeeded — alongside whatever structured fields (brand name, expiry date, classification) it managed to extract. The backend never re-runs OCR on it; it persists it so the caretaker always has something to look at later, whether the scan resolved cleanly or ended up pending.
- Does **not** contain REQ-02–REQ-17 business logic — beyond the on-device OCR/classification step above, it calls the backend API and renders what comes back.
- **No manual-data-entry screen exists anywhere in this app** ([REQ-17](../Requirements/REQ-17-caretaker-review-and-override.md)) — if a scan can't be confidently processed, the app has nothing further to ask; it's flagged pending review for the Web UI instead. This holds during REQ-15 onboarding too, not just day-to-day use.
- Caretaker-assisted onboarding ([REQ-15](../Requirements/REQ-15-assisted-onboarding.md)) happens here: account login, initial prescription/bill scans, escalation contact setup.
- Stays signed in persistently after onboarding (see [ARCH-02](ARCH-02-authentication.md)) — the elderly user never sees a login screen day-to-day.

## Web UI (Phase 2)

- Caretaker-facing dashboard ([REQ-10](../Requirements/REQ-10-caretaker-web-dashboard.md)): denser UI, ongoing medicine/contact management, adherence and escalation history.
- Login is with the caretaker's own account, not the elderly user's credentials. The landing screen is a list of every elderly user linked to that caretaker ([REQ-16](../Requirements/REQ-16-caretaker-multi-patient-linking.md)), from which one is selected to view/manage.
- **Review queue** ([REQ-17](../Requirements/REQ-17-caretaker-review-and-override.md)): every scan that ended up pending — whether the Android app's own OCR/classification couldn't confidently read it, or the backend couldn't resolve the medicine from what Android sent — shows up here with its original image, for the caretaker to type in the correct data. This is the only place structured medicine data is ever manually entered. Because every scan's image is stored regardless of outcome (see the Android app section above), a caretaker could also browse *resolved* scans' images later if needed, not just the pending ones.
- **Dosage/reminder override** ([REQ-17](../Requirements/REQ-17-caretaker-review-and-override.md)): the caretaker can view and correct the dosage/reminder currently active for any linked elderly user's medicine; a caretaker-entered value outranks every automatic dosage source (REQ-04/REQ-08).
- Talks to the same backend API as the Android app — no separate backend, no duplicated logic.
- This is where caretaker-facing complexity is intentionally concentrated, keeping the Android app minimal (per REQ-15's decision that caretakers shouldn't routinely return to the app to manage medicines).

## Backend (Python, FastAPI)

### API layer
- REST endpoints grouped by requirement domain: `/scan` (receives the image + whatever Android already extracted — classification itself already happened on-device, REQ-01), `/medicines`, `/prescriptions`, `/bills`, `/reminders`, `/escalation-contacts`, and (Phase 3) `/chat`.
- Thin — validates requests, calls the service layer, shapes responses. No business logic here.

### Service layer
- Where REQ-02–REQ-17 actually live: chemical-equivalence matching (REQ-02), dosage source fallback chain including the caretaker-override tier (REQ-04), prescription/bill extraction fallback chains ending in pending-review rather than manual entry (REQ-05/REQ-07), refill math (REQ-08), interaction/expiry checks (REQ-12/REQ-14), escalation rules (REQ-13), caretaker review/override handling (REQ-17). Scan classification (REQ-01) itself runs on Android, not here — this layer works from whatever Android already determined.
- Framed as independent services per domain so they can be tested and reasoned about in isolation, but all reachable from the API layer.

### Scheduler
- Runs reminder and escalation checks independent of whether a client is connected (APScheduler or Celery+Redis — exact choice deferred).
- Reads due reminders/escalations from PostgreSQL, triggers delivery (in-app push to Android for reminders, SMS for escalation per REQ-13).

### Integration adapters
- Thin wrappers around external services: translation, TTS (REQ-03, Phase 1), and (Phase 3) the AI chat model (REQ-09, Sarvam AI prioritized). OCR is **not** here — it runs on-device in the Android app (see the Android app section above), since it's consumed by exactly one frontend.
- Exist specifically so the service layer never hardcodes a vendor — swapping a translation provider, for instance, only touches its adapter.

## Data layer

- **PostgreSQL** — see [ARCH-03](ARCH-03-data-model.md) for entities. Holds both static reference data (medicine/chemical/interaction data) and per-user state (scans, reminders, adherence, escalation contacts).
