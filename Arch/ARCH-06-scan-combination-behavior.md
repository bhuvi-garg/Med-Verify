# ARCH-06 — Scan Combination Behavior

Status: Draft — pending review

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

## Corner cases to design against

- **Order independence**: a bill scanned before its prescription must produce the same end state as a prescription scanned before its bill, once both exist. The resolution logic must not assume scan order.
- **Brand-swap linking**: patient is prescribed "Paracetamol" but buys "Calpol." The bill's `USER_MEDICINE` must resolve to the *same* row as the prescription's, via chemical-component match (REQ-02), not brand-name text match — otherwise refill reminders silently stay in the "best-effort" state forever, believing no prescription exists.
- **Conflicting/updated prescriptions**: a second prescription is scanned for a medicine that already has one on file (e.g. a dosage change at a follow-up visit). Open question: does the newer prescription replace the dosage/schedule outright, or do both need to be reconciled (e.g. flagged for manual confirmation)? Silently overwriting risks losing a caretaker's ability to notice the change; always creating a duplicate `USER_MEDICINE` risks splitting reminders/history across two rows.
- **Course completion vs. ongoing refill need**: REQ-06 stops intake reminders when the prescription's course ends. Does REQ-08's refill reminder also stop at that point, or does it keep firing (e.g. for a chronic medication the patient keeps buying without a fresh prescription each time)? As written, REQ-08 has no course-end concept at all — needs a decision before this becomes an actual bug (nagging a patient to refill a course that's already finished).
- **Expired medicine short-circuits everything**: per REQ-14, an expired medicine never gets a dosage suggestion. This document's matrix assumes a non-expired medicine — expiry check must run *before* any row above is applied, not alongside it, so an expired-but-prescribed medicine doesn't still show "patient's own prescribed dosage."
- **Partial/failed identification**: if a bill or prescription line item can't be matched to any `USER_MEDICINE` (REQ-05/REQ-07's manual-entry fallback), it must not silently create an orphaned record with no chemical identity — orphaned records can't ever be linked up later, permanently stuck in the least-informed row of the matrix above.
- **Manual entry vs. later successful scan**: if manual entry was used for a medicine (because OCR/matching failed) and a later scan of the *same* medicine succeeds normally, the two must resolve to one `USER_MEDICINE`, not two — otherwise duplicate reminders fire for what the patient experiences as one medicine.

## Open questions

- Conflicting-prescription resolution (replace vs. reconcile vs. flag for caretaker review) — needs a decision before Design.
- Whether refill reminders (REQ-08) have any course-completion concept, or run indefinitely off the last known bill/dosage — needs a decision, and likely a REQ-08 amendment once decided.
