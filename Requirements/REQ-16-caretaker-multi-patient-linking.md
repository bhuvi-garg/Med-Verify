# REQ-16 — Caretaker Account & Multi-Patient Linking

Status: Approved — **deferred to Phase 2, not in initial scope**

## Description

The caretaker has their own account, separate from any elderly user's account, and can be linked to more than one elderly user. This lets a caretaker looking after multiple relatives (e.g. both parents, or several patients in a professional-caregiver setting) track and manage all of them from a single login on the [REQ-10](REQ-10-caretaker-web-dashboard.md) web dashboard, instead of needing a separate login per patient.

## Multiple caretakers per elderly user (decided)

More than one caretaker account can be linked to the same elderly user — e.g. two siblings sharing caretaking duties for the same parent. This is explicitly supported, not just tolerated.

## How linking happens (decided)

Linking is always **initiated from the elderly side**, not by a caretaker unilaterally claiming a patient from the dashboard:

1. **At elderly onboarding time (REQ-15)**: while setting up the elderly user's device, the first caretaker provides their own identifier (e.g. email/phone) to link right away.
2. **Adding a second (or later) caretaker, once one is already linked**: the *existing* linked caretaker adds the new caretaker directly from the Web UI (REQ-10) — they don't need to go back to the elderly user's device to do this.
3. **Adding a caretaker from the elderly-facing Android app directly**: also supported, specifically for the handover case — e.g. the original caretaker is no longer available (leaving the job, family circumstances change) and someone new needs to be linked without an existing caretaker account to do it from the Web UI side.
4. Arriving at REQ-10's dashboard shows every elderly user currently linked to that caretaker's account.

## Access control (decided for now, flagged as a security risk)

For now, providing a valid identifier is **sufficient** to complete a link — there is no additional approval step from the elderly user or from an existing caretaker before the new caretaker gains access. **This is a known security risk**, not an oversight: it means anyone who knows/guesses a valid caretaker identifier and an elderly account's linking detail could self-link to medical data with no gatekeeping. This is explicitly under deliberation and slated for a proper fix in Phase 2 (e.g. requiring confirmation from an existing caretaker or the elderly side before a new link becomes active) — it should not be treated as settled just because it's simple to build first.

## Acceptance criteria

- A caretaker account is distinct from any elderly user's account, with its own credentials.
- A single caretaker account can be linked to multiple elderly user accounts, **and** a single elderly user can have multiple linked caretaker accounts.
- Linking can happen during REQ-15 onboarding (optional, non-blocking), from the elderly-facing Android app afterward (e.g. for a caretaker handover), or from the Web UI by an already-linked caretaker adding another.
- No approval step currently gates a new link beyond providing a valid identifier — tracked as a known security gap to close in Phase 2, not a final design decision.
- The REQ-10 dashboard's entry point, after login, is a list of all elderly users linked to the caretaker's account, from which the caretaker selects one to view or manage.

## Open questions

- What the eventual approval/verification mechanism should look like once the current "valid identifier is sufficient" approach is revisited in Phase 2.
