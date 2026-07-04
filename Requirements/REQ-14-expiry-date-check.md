# REQ-14 — Expiry Date Check

Status: Draft — pending review

## Description

When a medicine is scanned (REQ-01/REQ-02), the system also reads its printed expiry date and warns the user if the medicine is expired or close to expiring.

## Thresholds and behavior (decided)

- "Nearing expiry" warning triggers **1 week before** the printed expiry date.
- If the medicine is already expired, REQ-04's dosage suggestion is **never shown** — the system tells the user the medicine has expired and that they need to get a new one instead. The app must never suggest a dosage for an expired medicine, no exceptions.

## Acceptance criteria

- Given a scanned medicine, the system extracts the printed expiry date, where legible.
- If the medicine is already expired, the system shows a clear expired warning (per REQ-11 sizing) instructing the user to get a new one, and suppresses REQ-04's dosage suggestion entirely for that scan.
- If the medicine is within 1 week of its expiry date, the system surfaces a milder "expiring soon" warning alongside the normal dosage suggestion.
