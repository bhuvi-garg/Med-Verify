# REQ-13 — Missed-Dose Escalation

Status: Approved

## Description

Building on adherence tracking in [REQ-06](REQ-06-dosage-reminder.md), if a user repeatedly fails to acknowledge/take a scheduled dose, the system escalates by notifying one saved emergency/family contact — without requiring the full caretaker web dashboard (REQ-10, Phase 2).

## Threshold, channel, and consent (decided)

- The missed-dose threshold that triggers escalation is **per-medicine**, not a single fixed global value — e.g. a medicine with tighter safety margins may escalate after 1 missed dose, while others tolerate more. The specific per-medicine thresholds are a Design/data decision.
- Notification channel is **SMS** for Phase 1, plus the caretaker's **web dashboard** (REQ-10) once that ships in Phase 2.
- Consent: the caretaker sets up escalation contacts **unilaterally** during onboarding (REQ-15) — the elderly user does not need to separately approve this.

## Acceptance criteria

- The caretaker (per REQ-15) can save one or more contacts for escalation during setup.
- If a scheduled dose reminder goes unacknowledged beyond the applicable per-medicine threshold, the system sends an SMS to the saved contact.
- Once REQ-10 ships, the same escalation events are also visible in the caretaker's web dashboard.

## Open questions

- The actual per-medicine thresholds (count/time) need a data source — likely the same reference data used for REQ-04/REQ-12 dosage and interaction info. To be defined in Design.
