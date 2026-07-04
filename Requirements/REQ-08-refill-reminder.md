# REQ-08 — Medicine Refill Reminder

Status: Approved

## Description

When a pharmacy bill has been scanned and processed (REQ-07), the system calculates how many days the purchased quantity will last, and reminds the user to buy a refill 5 days before the medicine is expected to run out.

Per [REQ-00](REQ-00-behavior-model.md), the dosage used for this calculation comes from whichever source is available, following the same priority order as [REQ-04](REQ-04-dosage-suggestion.md):

1. A caretaker-verified override for this medicine, if one has been set ([REQ-17](REQ-17-caretaker-review-and-override.md), Phase 2).
2. A dosage previously extracted from a scanned prescription for this medicine (REQ-05), if known.
3. Otherwise, the standard/common dosage (REQ-04), as a best-effort estimate.
4. If none of the above are available, the system cannot calculate a run-out date — no refill reminder is created, and this is not an error state.

## Acceptance criteria

- Given a purchased quantity (REQ-07) and an available dosage (caretaker-override, prescription-based, or default), the system calculates the expected run-out date and reminds the user 5 days before it.
- The reminder indicates which dosage source was used, so the user understands its accuracy.
- If no dosage information is available at all, no reminder is created.

## Dependencies / risks

- Accuracy depends entirely on which dosage source was used; a default-estimate-based reminder may be less accurate than a prescription-based one.

## Persistence (confirmed)

Yes — per [REQ-00](REQ-00-behavior-model.md), prescription dosage data persists across sessions per user/medicine. A bill scanned before its matching prescription still gets a best-effort reminder (default dosage), and that reminder is automatically upgraded once the matching prescription is scanned later.

## Course completion (decided)

Refill reminders stop when the prescription's course ends, the same course-duration data [REQ-06](REQ-06-dosage-reminder.md) uses to stop intake reminders — the doctor has already written how long the medicine should be taken, so that's the source of truth for both, not two separately-tracked durations.

- If the prescription specifies a fixed number of days, refill reminders stop once that course completes (no nagging a patient to refill a course that's already finished).
- If the medicine is chronic/ongoing (no end date by design — e.g. a long-term daily medication), refill reminders continue indefinitely once that's been established.
- If the course duration wasn't captured from the prescription scan at all, it's flagged for the caretaker to populate via the Web UI ([REQ-17](REQ-17-caretaker-review-and-override.md)) — either a specific number of days, or a confirmation that it's indefinite. Until populated, refill reminders keep firing as usual rather than silently stopping (an unnecessary reminder is a far smaller risk than a missed refill), but the gap is surfaced to the caretaker rather than left unresolved forever.

## Acceptance criteria (course completion)

- Refill reminders stop automatically once the linked prescription's course duration elapses, matching REQ-06's intake-reminder cutoff.
- Refill reminders continue indefinitely for a medicine explicitly marked chronic/ongoing.
- If course duration is unknown, refill reminders are not suppressed, but the gap is flagged for caretaker input (fixed days vs. indefinite) via REQ-17.
