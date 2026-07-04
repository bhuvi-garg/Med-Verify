# ARCH-02 — Authentication

Status: Approved

## Model

Account-based, not a bare device ID — decided because the caretaker is already present and involved during onboarding ([REQ-15](../Requirements/REQ-15-assisted-onboarding.md)), so credential setup costs little extra effort, and it gives [REQ-10](../Requirements/REQ-10-caretaker-web-dashboard.md)'s Phase 2 caretaker web login a proper foundation instead of requiring a migration later.

## Two account kinds (decided)

There are two distinct account types, not one shared login:

- **Elderly account** — created during REQ-15 onboarding, lives on the Android device, holds all of that patient's medicine/prescription/bill/reminder data. This is what Phase 1 fully needs.
- **Caretaker account** — the caretaker's own separate identity, introduced in Phase 2 alongside [REQ-10](../Requirements/REQ-10-caretaker-web-dashboard.md) and [REQ-16](../Requirements/REQ-16-caretaker-multi-patient-linking.md). A single caretaker account can be **linked** to multiple elderly accounts, and a single elderly account can have **multiple** linked caretaker accounts (e.g. two siblings) — rather than caretakers sharing the elderly user's credentials. See ARCH-03's `CARETAKER_ACCOUNT`/`CARETAKER_LINK` entities.

## Flow

1. **Onboarding (REQ-15)**: the caretaker creates the elderly account (username/password) on the elderly user's Android device, as part of initial setup. They may optionally also provide their own caretaker identifier at this point to request a link (REQ-16) — this never blocks completing onboarding.
2. **Session persistence**: the backend issues a session/refresh token for the elderly account, stored in the Android Keystore. The app stays signed in indefinitely — the elderly user is never shown a login screen during normal day-to-day use.
3. **Optional local unlock**: a biometric (fingerprint) prompt can gate opening the app on-device. This is a local convenience layer only — it does not replace or bypass the backend's own auth, it just guards access to the already-authenticated session.
4. **Phase 2 caretaker login**: the caretaker logs into the Web UI with their **own** caretaker account (not the elderly user's credentials), lands on a list of every elderly account linked to them (REQ-16), and picks one to view/manage — reaching the same backend data as the Android app for that patient.
5. **Adding another caretaker, initiated from the elderly side (decided)**: a new link is always initiated from the elderly side, never unilaterally claimed from the dashboard. Concretely: (a) an *already-linked* caretaker can add a second caretaker directly from the Web UI, or (b) the elderly-facing Android app itself can initiate adding a caretaker — specifically for a handover (the original caretaker is no longer available and a new one takes over) when no existing caretaker account is available to do it from the Web UI side.
6. **No approval gate yet (decided, flagged as a security risk)**: today, supplying a valid caretaker identifier is sufficient to complete a link — there's no confirmation step from the elderly user or an existing caretaker. This is intentionally called out as a gap to close in Phase 2, not a settled design; see REQ-16 for the full reasoning.

## Acceptance criteria

- Elderly account creation requires no elderly-user interaction — the caretaker performs it during REQ-15 onboarding.
- After onboarding, the Android app requires no further login prompts for normal use.
- Session/refresh tokens are stored securely on-device (Android Keystore), not in plain storage.
- A caretaker account is distinct from any elderly account and authenticates separately; it can be linked to more than one elderly account, and an elderly account can have more than one linked caretaker.
- New links can be initiated either from the elderly-facing Android app or, once one caretaker is already linked, from that caretaker's Web UI session — not by an unlinked caretaker self-serving access from the dashboard.
- Reaching the Web UI in Phase 2 requires no data migration — the caretaker account model and its link table exist in the data layer from the start (ARCH-03), even before the Web UI itself ships.

## Open questions

- **Known security gap (tracked, not yet fixed)**: linking currently requires only a valid identifier, with no approval step. A proper fix (e.g. confirmation from an existing caretaker or the elderly side) is planned for Phase 2 — see REQ-16.
- Password reset/account recovery flow if the caretaker forgets credentials — not yet defined.
