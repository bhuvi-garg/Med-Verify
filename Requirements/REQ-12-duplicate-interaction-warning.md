# REQ-12 — Duplicate Medicine / Interaction Warning

Status: Approved

## Description

Building on the chemical-identity matching in [REQ-02](REQ-02-medicine-identification.md), the system should warn the user when:

- Two medicines they have on file (from any combination of scans) share the same active chemical component(s) under different brand names — risking accidental double-dosing (e.g. taking both Calpol and Dolo, not realizing both are Paracetamol).
- A known dangerous interaction exists between two of the user's medicines.

## Data source & trigger (decided)

Interaction data uses the same local DB + online fallback pattern as REQ-02/REQ-04. The check runs **automatically** on every relevant scan — the user is not required to explicitly request it, consistent with keeping elderly-facing interaction minimal (REQ-11).

## Acceptance criteria

- When a newly scanned/identified medicine shares an active component with another medicine already on file for this user, the system automatically surfaces a duplicate-ingredient warning before/alongside any dosage suggestion.
- When two medicines on file have a known interaction (per the local DB, falling back to an online lookup as in REQ-02/REQ-04), the system automatically surfaces an interaction warning.
- Warnings are clear, elderly-readable (per REQ-11), and explain *why* (e.g. "Both contain Paracetamol").
- No user action is required to trigger this check — it happens as a side effect of a normal scan.
