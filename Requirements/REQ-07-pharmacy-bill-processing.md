# REQ-07 — Pharmacy Bill Processing

Status: Draft — pending review

## Description

When the scanned input is a pharmacy bill (REQ-01), the system extracts, per medicine listed on the bill: medicine name, quantity/pack size purchased, and purchase date.

This is an independent scan — it does not require a prescription or medicine scan to have happened first. See [REQ-00](REQ-00-behavior-model.md).

## Acceptance criteria

- Given a scanned pharmacy bill, the system extracts medicine name, quantity purchased, and purchase date for each line item, and populates the working database with one entry per medicine on the bill.
- If a medicine listed on the bill can't be matched/identified, the system prompts for manual entry rather than dropping the line item.
- Extracted data feeds into REQ-08 (refill reminder) when enough information is available to calculate a run-out date.
