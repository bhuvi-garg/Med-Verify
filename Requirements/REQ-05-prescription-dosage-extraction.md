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
3. If that still doesn't resolve it, prompt for manual entry of the information by a person (e.g. the patient or caretaker).

## Open questions

- None outstanding — see fallback chain above.
