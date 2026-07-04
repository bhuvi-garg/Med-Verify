# ARCH-02 — Authentication

Status: Draft — pending review

## Model

Account-based, not a bare device ID — decided because the caretaker is already present and involved during onboarding ([REQ-15](../Requirements/REQ-15-assisted-onboarding.md)), so credential setup costs little extra effort, and it gives [REQ-10](../Requirements/REQ-10-caretaker-web-dashboard.md)'s Phase 2 caretaker web login a proper foundation instead of requiring a migration later.

## Flow

1. **Onboarding (REQ-15)**: the caretaker creates the account (username/password) on the elderly user's Android device, as part of initial setup.
2. **Session persistence**: the backend issues a session/refresh token, stored in the Android Keystore. The app stays signed in indefinitely — the elderly user is never shown a login screen during normal day-to-day use.
3. **Optional local unlock**: a biometric (fingerprint) prompt can gate opening the app on-device. This is a local convenience layer only — it does not replace or bypass the backend's own auth, it just guards access to the already-authenticated session.
4. **Phase 2 caretaker login**: the caretaker logs into the Web UI using the same account credentials (or a linked caretaker sub-account — see open question below), reaching the same backend data as the Android app.

## Acceptance criteria

- Account creation requires no elderly-user interaction — the caretaker performs it during REQ-15 onboarding.
- After onboarding, the Android app requires no further login prompts for normal use.
- Session/refresh tokens are stored securely on-device (Android Keystore), not in plain storage.
- The same account (or an account explicitly linked to it) can authenticate to the Web UI in Phase 2 without any data migration.

## Open questions

- Should the caretaker have their own separate login (a "caretaker sub-account" linked to the elderly user's account), or do they simply share the elderly user's own credentials? This matters for REQ-13's escalation contacts and audit trail (who changed what) — to be decided before Phase 2 Design.
- Password reset/account recovery flow if the caretaker forgets credentials — not yet defined.
