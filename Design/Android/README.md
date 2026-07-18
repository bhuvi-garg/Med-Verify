# Design — Android

UI/UX design for the elderly-facing Android app (Phase 1): screen list, navigation flow, and layout decisions.

Must respect two hard constraints from [Requirements/](../../Requirements/README.md):

- **[REQ-11](../../Requirements/REQ-11-simplified-ui-mode.md)** — exactly one always-simplified UI, no denser mode ever appears here.
- **[REQ-17](../../Requirements/REQ-17-caretaker-review-and-override.md)** — no manual data-entry screen anywhere in this app.

Ground the screen list in [Arch/ARCH-05](../../Arch/ARCH-05-flows.md)'s Onboarding and Scan flows — they map closely to what screens/navigation are actually needed.

- **[units/](units/README.md)** — the two units that run on-device (OCR, Classification), per [Arch/ARCH-00](../../Arch/ARCH-00-overview.md#why-not-put-logic-on-device-and-the-one-exception)'s single-consumer test. Everything else the app needs is served by [Design/Backend/units/](../Backend/units/README.md).

Screen/navigation design (beyond the two units above) is not started yet.
