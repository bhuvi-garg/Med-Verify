# REQ-15 — Assisted/Caretaker-Led Onboarding

Status: Draft — pending review

## Description

First-time setup is often the biggest drop-off point for elderly users. This requirement lets a family member/caretaker perform the initial setup on the elderly user's behalf — before the full caretaker web dashboard (REQ-10, Phase 2) exists — directly on the elderly user's own device.

## Scope of what the caretaker does here (decided)

The caretaker's role in this app (as opposed to the future REQ-10 web dashboard) is deliberately narrow:

- Set up the account/device for the elderly user.
- Scan the initial prescription(s) and pharmacy bill(s) through the app's normal scan flow (REQ-01/REQ-05/REQ-07) — same as the elderly user would use day to day.
- Manually enter details only when the system fails to read/identify something (the same manual-entry fallback defined in REQ-05/REQ-07), not as a routine data-entry task.
- Set up and, going forward, update the escalation contact(s) (REQ-13) at any time.

What the caretaker should **not** do here: routinely come back to manually add new medicines one by one. That kind of ongoing management belongs in the REQ-10 web dashboard, which is exactly why that dashboard exists — to hold the caretaker-facing complexity so this app can stay simple enough that the elderly user is never at risk of landing on the wrong screen and getting stuck.

## Acceptance criteria

- A setup flow exists where a caretaker can, on the elderly user's device: set up the account, scan initial prescriptions/bills, and set up escalation contacts.
- The caretaker can return at any time to update the escalation contact(s), but the flow does not encourage or expect repeat visits to add medicines individually — that responsibility moves to REQ-10 once it exists.
- Once set up, day-to-day use (scanning, reminders) requires no login/account complexity for the elderly user themselves (consistent with REQ-00/REQ-01 assuming Phase 1 is single-user/local, no login).
- The setup flow itself can use a denser/standard UI (since the caretaker is performing it), distinct from the always-simplified elderly-facing mode (REQ-11).
