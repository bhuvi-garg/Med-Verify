# REQ-05 — Prescription Dosage Extraction

Status: Approved

## Description

When the scanned input is a prescription (REQ-01), the system must read it and extract the dosage instructions the doctor has written (which medicine, how much, and how often).

## Acceptance criteria

- Given a scanned prescription, the system extracts, per medicine listed: name, dosage amount, and frequency/timing.
- Extracted dosage feeds into REQ-06 (intake reminders) and, if a pharmacy bill for the same medicine is separately known, REQ-08 (refill reminders) — see [REQ-00](REQ-00-behavior-model.md) on how independent scans relate.

## Fallback chain for poor legibility / unidentifiable entries (decided)

Handwritten prescriptions won't always be cleanly readable. The system follows this fallback order per medicine line item:

1. Attempt to read/extract it directly from the prescription scan.
2. If unreadable, or the medicine can't be identified, cross-check against a pharmacy bill already on file for this patient (see [REQ-00](REQ-00-behavior-model.md) persistence model) to infer what it likely is.
3. If that still doesn't resolve it, the line item is flagged as **pending caretaker review** rather than prompting anyone to type it in on the Android device — the Android app never asks for manual data entry (see [REQ-17](REQ-17-caretaker-review-and-override.md)). Per [REQ-00](REQ-00-behavior-model.md)'s silent-skip principle, no dosage suggestion or reminder is created for that line item until it's resolved.

## A new prescription always wins, unless a caretaker override is active (decided)

If a medicine already has a prescription on file and a **newer** prescription is scanned for it, the new one simply replaces the old one automatically — no confirmation, no conflict, no caretaker involvement needed. This holds even if the earlier prescription entry only exists because a caretaker helped resolve an unreadable scan (fallback step 3 above, or a REQ-17 pending-review resolution) — a caretaker filling in what an illegible prescription said is not the same thing as a caretaker **override** ([REQ-17](REQ-17-caretaker-review-and-override.md)), and does not block a later prescription from automatically taking over. A newly scanned prescription is always the source of truth here.

The **only** thing that requires explicit caretaker confirmation before a new prescription takes effect is an actual `DOSAGE_OVERRIDE` — a caretaker deliberately correcting the active dosage/reminder, as opposed to just transcribing what a prescription says. See REQ-17's conflict-handling rule for that case specifically.

## Open questions

- None outstanding — see fallback chain and prescription-precedence rule above.
