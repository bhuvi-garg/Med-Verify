# ARCH-06 — Scan Combination Behavior

Status: Approved

## Why this document exists

[REQ-00](../Requirements/REQ-00-behavior-model.md) through [REQ-08](../Requirements/REQ-08-refill-reminder.md) each define a fallback rule in isolation (dosage source order, prescription-legibility fallback, refill dosage source, etc.). What's missing from the requirements is a single place showing what actually happens for every *combination* of scans a real patient could produce, in whatever order they happen to arrive. This is where the corner cases live — most bugs in this system will come from an unanticipated combination, not from a single scan type in isolation.

Everything below resolves around one identity: [`USER_MEDICINE`](ARCH-03-data-model.md), keyed by chemical component set (REQ-02), not by scan type or brand name. Every rule assumes the service layer has already resolved "which `USER_MEDICINE` row does this scan belong to" before applying dosage/reminder logic.

## Combination matrix

| Data on file for a `USER_MEDICINE` | Dosage suggestion (REQ-04) | Intake reminder (REQ-06) | Refill reminder (REQ-08) |
|---|---|---|---|
| **Medicine only** | Standard elderly dosage (local DB → online) | None — no schedule to base it on | None — no quantity/dosage to base it on |
| **Prescription only** | Patient's own prescribed dosage | Yes, per prescription frequency, ends at course completion | None — no purchase quantity on file yet |
| **Bill only** | Standard elderly dosage (no prescription yet) | None | Best-effort, using standard dosage as the estimate |
| **Medicine + Prescription** | Patient's own prescribed dosage | Yes | None — still no bill/quantity |
| **Medicine + Bill** | Standard elderly dosage | None | Best-effort, same as bill-only |
| **Prescription + Bill** | Patient's own prescribed dosage | Yes | Accurate — real dosage × real quantity |
| **All three** | Patient's own prescribed dosage | Yes | Accurate |

Per [REQ-00](../Requirements/REQ-00-behavior-model.md), these rows are not static — a `USER_MEDICINE` moves down this table over time as more scans arrive, and any reminder already scheduled is **updated in place**, not recreated, when a better data source becomes available (e.g. bill-only's best-effort refill reminder gets its dosage swapped from "estimated" to "prescribed" the moment a matching prescription is scanned).

This table also has an orthogonal override dimension not shown as a row: if a caretaker has set a `DOSAGE_OVERRIDE` for this `USER_MEDICINE` ([REQ-17](../Requirements/REQ-17-caretaker-review-and-override.md), Phase 2), it wins the "Dosage suggestion" column outright, regardless of which row the medicine is otherwise in — even "Prescription + Bill"/"All three" defer to it. Intake and refill reminders are unaffected by a dosage override on their own (a caretaker can also edit those directly, but that's a separate action, not implied by setting a dosage override).

## Corner cases to design against

- **Order independence**: a bill scanned before its prescription must produce the same end state as a prescription scanned before its bill, once both exist. The resolution logic must not assume scan order.
- **Brand-swap linking**: patient is prescribed "Paracetamol" but buys "Calpol." The bill's `USER_MEDICINE` must resolve to the *same* row as the prescription's, via chemical-component match (REQ-02), not brand-name text match — otherwise refill reminders silently stay in the "best-effort" state forever, believing no prescription exists.
- **Conflicting/updated prescriptions, no override involved (resolved)**: a second prescription is scanned for a medicine that already has one on file, with no caretaker override in play (e.g. a dosage change at a routine follow-up visit). The new prescription **always wins automatically** — no confirmation, no conflict (REQ-05). This applies even when the existing entry only exists because a caretaker helped resolve an illegible earlier scan (REQ-17's review queue) — that's transcription, not an override, so it never blocks a later prescription from taking over normally. Only an actual `DOSAGE_OVERRIDE` triggers the conflict flow below.
- **Course completion vs. ongoing refill need (resolved)**: REQ-08's refill reminder now shares the exact same course-completion source as REQ-06's intake reminder — the prescription's `duration_status`/`course_duration_days` (ARCH-03) — rather than tracking its own end condition. Fixed-duration courses stop both reminder types together; chronic/ongoing medicines keep both running; and if the duration was never captured, both keep firing (safer than silently stopping) while it's flagged for the caretaker to populate via REQ-17.
- **Expired medicine short-circuits everything**: per REQ-14, an expired medicine never gets a dosage suggestion. This document's matrix assumes a non-expired medicine — expiry check must run *before* any row above is applied, not alongside it, so an expired-but-prescribed medicine doesn't still show "patient's own prescribed dosage."
- **Partial/failed identification**: if a bill or prescription line item can't be matched to any `USER_MEDICINE`, it becomes a `SCAN_ARTIFACT` flagged `pending` (REQ-05/REQ-07/REQ-17) rather than an on-device manual-entry prompt or a silently-created orphaned record. It must not be written as a bare, chemical-identity-less row that can never be linked up later — it needs to stay in the review queue until a caretaker resolves it via the Web UI, however long that takes (in Phase 1, potentially indefinitely — see ARCH-05's note on this gap).
- **Caretaker-resolved artifact vs. later successful scan**: if a caretaker resolves a `SCAN_ARTIFACT` for a medicine (because OCR/matching originally failed), and a later scan of the *same* medicine succeeds normally on its own, the two must resolve to one `USER_MEDICINE`, not two — otherwise duplicate reminders fire for what the patient experiences as one medicine.
- **Override outliving its source (resolved)**: a `DOSAGE_OVERRIDE` can never silently keep suppressing a newer prescription — per REQ-17, any new `PRESCRIPTION_ITEM` scanned against a `USER_MEDICINE` with an active override raises a `PRESCRIPTION_CONFLICT_REVIEW` and forces an explicit caretaker decision (with a reason) before anything changes. This is the **only** case anywhere in this matrix that pauses for confirmation — every other prescription update (see above) applies automatically.

## Open questions

- None outstanding — all corner cases above are resolved.
