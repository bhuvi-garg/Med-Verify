# REQ-08 — Medicine Refill Reminder

Status: Draft — pending review

## Description

When a pharmacy bill has been scanned and processed (REQ-07), the system calculates how many days the purchased quantity will last, and reminds the user to buy a refill 5 days before the medicine is expected to run out.

Per [REQ-00](REQ-00-behavior-model.md), the dosage used for this calculation comes from whichever source is available, in order of preference:

1. A dosage previously extracted from a scanned prescription for this medicine (REQ-05), if known.
2. Otherwise, the standard/common dosage (REQ-04), as a best-effort estimate.
3. If neither is available, the system cannot calculate a run-out date — no refill reminder is created, and this is not an error state.

## Acceptance criteria

- Given a purchased quantity (REQ-07) and an available dosage (prescription-based or default), the system calculates the expected run-out date and reminds the user 5 days before it.
- The reminder indicates whether the dosage used was prescription-based or a default estimate, so the user understands its accuracy.
- If no dosage information is available at all, no reminder is created.

## Dependencies / risks

- Accuracy depends entirely on which dosage source was used; a default-estimate-based reminder may be less accurate than a prescription-based one.

## Persistence (confirmed)

Yes — per [REQ-00](REQ-00-behavior-model.md), prescription dosage data persists across sessions per user/medicine. A bill scanned before its matching prescription still gets a best-effort reminder (default dosage), and that reminder is automatically upgraded once the matching prescription is scanned later.
