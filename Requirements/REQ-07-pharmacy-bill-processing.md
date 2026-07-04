# REQ-07 — Pharmacy Bill Processing

Status: Approved

## Description

When the scanned input is a pharmacy bill (REQ-01), the system extracts, per medicine listed on the bill: medicine name, quantity/pack size purchased, and purchase date.

This is an independent scan — it does not require a prescription or medicine scan to have happened first. See [REQ-00](REQ-00-behavior-model.md).

## Acceptance criteria

- Given a scanned pharmacy bill, the system extracts medicine name, quantity purchased, and purchase date for each line item, and populates the working database with one entry per medicine on the bill.
- If a medicine listed on the bill can't be matched/identified, the line item is flagged as **pending caretaker review** rather than dropped, and rather than prompting for manual entry on the Android device — the Android app never asks for manual data entry (see [REQ-17](REQ-17-caretaker-review-and-override.md)). No refill reminder is calculated from it until it's resolved (REQ-00's silent-skip principle).
- Extracted data feeds into REQ-08 (refill reminder) when enough information is available to calculate a run-out date.
