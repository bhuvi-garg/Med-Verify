# Med-Verify

Med-Verify scans medicines, prescriptions, and pharmacy bills to help patients — especially elderly users — understand what they're taking, how much of it, and when.

It reads what's printed on a medicine's packaging back to the user in large text and audio, in their own local language; extracts dosage schedules from prescriptions and reminds the user when to take each dose; and tracks pharmacy bills to remind them to refill before running out.

## Who it's for

The app is designed around **elderly users** as the primary audience:

- A single, always-simplified interface — large text, large touch targets, minimal steps per screen.
- No login or account complexity for the elderly user in Phase 1.
- A caretaker (family member) can perform initial setup and manage escalation contacts, but day-to-day use is meant to be simple enough that the elderly user never gets stuck on the wrong screen.
- A separate caretaker web dashboard, with more detailed tracking and management, is planned for Phase 2.

## What it does

Every scan is exactly one of three independent input types — there's no combined input:

| Scan | What happens |
|---|---|
| **Medicine** | Identifies the medicine by its chemical/active ingredient (so different brands of the same drug are recognized as equivalent), reads the label back in large text and translated audio, and suggests a standard elderly-appropriate dosage. |
| **Prescription** | Extracts the dosage and schedule the doctor wrote, and sets up intake reminders. |
| **Pharmacy Bill** | Extracts the medicine and quantity purchased, and — combined with whatever dosage information is known — sets up a refill reminder before the supply runs out. |

The app remembers everything it learns per medicine, across separate scans, and fills in gaps as more information becomes available (e.g. a bill scanned before its prescription still gets a best-effort refill reminder, which is automatically corrected once the prescription is scanned later).

It also warns about duplicate/interacting medicines, checks expiry dates, and can escalate to a caretaker if doses are repeatedly missed. An AI chat box for follow-up questions (prioritizing Indian-language-first models like Sarvam AI) is planned for a later version.

## Repository structure

This repository is in the pre-implementation phase — requirements are being finalized before any design or code work starts.

- **[Requirements/](Requirements/README.md)** — the full set of requirements for the app, start here for what the app must do.
- `Design/` — architecture and UI/UX design (not yet started).
- `Arch/` — architecture artifacts/diagrams (not yet started).
- `Implementation/` — source code (not yet started).
- `Test/` — test plans and test code (not yet started).
- `.claude/` — Claude Code project-local config.
