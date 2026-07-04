# REQ-11 — Simplified/Large-UI Mode

Status: Approved

## Description

The app targets elderly users, so the entire app — not just label reading (REQ-03) — should default to a simplified, large-UI presentation: large text, large touch targets, high contrast, and minimal steps per screen, so the app is usable without fine motor control or strong eyesight.

## Scope (decided)

The elderly-facing app has **one** UI: the simplified/large mode, always — there is no toggle to a denser mode inside the app. Any denser/complex UI for caretakers lives entirely in the separate web dashboard (REQ-10), not in this app. This keeps the elderly user's app free of screens they could get lost in.

## Acceptance criteria

- Text size, button size, and touch-target size across all screens meet elderly-accessibility sizing (not just the label-reading output from REQ-03).
- Each screen presents a single, clear primary action rather than dense menus/options.
- High-contrast display is available as a mode, independent of device dark/light theme.
- The app never exposes a denser/advanced UI mode — that complexity is confined to the web dashboard (REQ-10).

## Open questions

- Specific sizing/contrast targets (e.g. WCAG AA/AAA) — deferred to Design.
