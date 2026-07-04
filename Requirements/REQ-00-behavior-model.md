# REQ-00 — Data Availability & Fallback Behavior

Status: Draft — pending review

## Description

This requirement is cross-cutting and applies to every other requirement in this folder.

The app supports three **independent** scan types — Medicine, Prescription, and Pharmacy Bill (REQ-01). There is no combined "prescription + bill" input: each is scanned on its own, at its own time, and the app is not guaranteed to have all three for a given medicine.

Because of this, the app must always act on **whatever knowledge it currently has**, and behave gracefully when something is missing:

- If a specific dosage is known (e.g. extracted from a scanned prescription, REQ-05), use it.
- If not, fall back to the standard/common dosage (REQ-04) where applicable.
- If a feature has no data to work with and no applicable default (e.g. a refill reminder with no bill and no prescription), the app silently skips that feature rather than erroring, blocking, or prompting the user to supply missing input.

## Persistent memory across scans (decided)

The app **does** persist everything it learns, per user and per medicine (identified by its chemical component — see [REQ-02](REQ-02-medicine-identification.md) on treating chemically-equivalent brands as the same medicine). Scans are independent events, but their extracted knowledge accumulates over time and later scans can complete or correct earlier ones. For example:

1. User uploads only a pharmacy bill for a medicine, with no prescription on file. The app stores what it extracted (medicine, quantity, purchase date) and, lacking a dosage, either uses the REQ-04 default or skips the refill reminder per REQ-08's fallback order.
2. Later, the user scans the prescription for that same medicine (by chemical identity). The app cross-verifies it against the stored bill and **updates** the existing refill reminder using the now-known prescription dosage instead of the default/skip.
3. Later still, the user buys a refill and scans the new bill. The app remembers the earlier prescription dosage and immediately produces an accurate refill reminder from it — the user does not need to re-scan the prescription.

## Acceptance criteria

- No requirement in this document set should assume all three scan types are present together.
- Every feature that depends on optional data (dosage, quantity, schedule) must define its fallback default and its "not enough data" behavior (skip silently) in its own requirement file.
- Knowledge extracted from any scan is persisted per user and per medicine (by chemical identity), and is reused by later scans of a different type for the same medicine — as in the scenario above.
- When a later scan supplies better data than what was previously known (e.g. a prescription arriving after a bill-only default), the app updates dependent outputs (e.g. an already-scheduled refill reminder) rather than leaving them stale.
