# Requirements — Med-Verify

Status: **Approved**

Each requirement is a separate file, numbered `REQ-00` onward. This index lists them grouped by the input type they apply to.

## Phasing

- **Phase 1** — the core Android app: all three scan types, dosage suggestions, intake/refill reminders, safety checks (duplicate/interaction, expiry), SMS escalation.
- **Phase 2** — the caretaker Web UI and everything that depends on it: multi-patient linking (REQ-16), and caretaker review/override of unresolved scans and dosage/reminders (REQ-17).
- **Phase 3** — the AI chat follow-up (REQ-09).

Phase 1 and Phase 2 are built and demoed together — every requirement in them is deterministic and scripted, with no probabilistic behavior. REQ-09 is the only requirement that's inherently probabilistic (an LLM's output isn't guaranteed reproducible), which is exactly why it's pushed out to its own Phase 3 rather than bundled into Phase 2 alongside the otherwise-deterministic Web UI work.

## Cross-cutting

- [REQ-00 — Data Availability & Fallback Behavior](REQ-00-behavior-model.md): the app supports three **independent** scan types with no combined input. It always acts on whatever it currently knows, falls back to defaults where defined, and silently skips anything it can't do — read this first, it governs how every other requirement below behaves when data is missing.

## Input types

Every scan is exactly one of these three — there is no "prescription + bill" combined input.

| Input scanned | Requirements |
|---|---|
| Medicine (pill/strip/label) | [REQ-01](REQ-01-input-classification.md), [REQ-02](REQ-02-medicine-identification.md), [REQ-03](REQ-03-label-reading.md), [REQ-04](REQ-04-dosage-suggestion.md) |
| Prescription | [REQ-01](REQ-01-input-classification.md), [REQ-05](REQ-05-prescription-dosage-extraction.md), [REQ-06](REQ-06-dosage-reminder.md) |
| Pharmacy Bill | [REQ-01](REQ-01-input-classification.md), [REQ-07](REQ-07-pharmacy-bill-processing.md), [REQ-08](REQ-08-refill-reminder.md) *(uses prescription dosage if known, otherwise the REQ-04 default)* |
| Follow-up (any) | [REQ-09](REQ-09-ai-chat-followup.md) *(deferred to Phase 3)* |
| Caretaker access (any) | [REQ-10](REQ-10-caretaker-web-dashboard.md) *(deferred to Phase 2)*, [REQ-15](REQ-15-assisted-onboarding.md), [REQ-16](REQ-16-caretaker-multi-patient-linking.md) *(deferred to Phase 2)*, [REQ-17](REQ-17-caretaker-review-and-override.md) *(deferred to Phase 2)* |
| Elderly usability (cross-cutting) | [REQ-11](REQ-11-simplified-ui-mode.md), [REQ-12](REQ-12-duplicate-interaction-warning.md), [REQ-13](REQ-13-missed-dose-escalation.md), [REQ-14](REQ-14-expiry-date-check.md) |

## All requirements

0. [REQ-00 — Data Availability & Fallback Behavior](REQ-00-behavior-model.md)
1. [REQ-01 — Input Classification](REQ-01-input-classification.md)
2. [REQ-02 — Medicine Identification](REQ-02-medicine-identification.md)
3. [REQ-03 — Label Reading (Text + Audio, Multi-language)](REQ-03-label-reading.md)
4. [REQ-04 — Common Dosage Suggestion](REQ-04-dosage-suggestion.md)
5. [REQ-05 — Prescription Dosage Extraction](REQ-05-prescription-dosage-extraction.md)
6. [REQ-06 — Dosage Intake Reminder](REQ-06-dosage-reminder.md)
7. [REQ-07 — Pharmacy Bill Processing](REQ-07-pharmacy-bill-processing.md)
8. [REQ-08 — Medicine Refill Reminder](REQ-08-refill-reminder.md)
9. [REQ-09 — AI Chat Follow-up](REQ-09-ai-chat-followup.md) *(deferred to Phase 3)*
10. [REQ-10 — Caretaker Web Dashboard](REQ-10-caretaker-web-dashboard.md) *(deferred to Phase 2)*
11. [REQ-11 — Simplified/Large-UI Mode](REQ-11-simplified-ui-mode.md)
12. [REQ-12 — Duplicate Medicine / Interaction Warning](REQ-12-duplicate-interaction-warning.md)
13. [REQ-13 — Missed-Dose Escalation](REQ-13-missed-dose-escalation.md)
14. [REQ-14 — Expiry Date Check](REQ-14-expiry-date-check.md)
15. [REQ-15 — Assisted/Caretaker-Led Onboarding](REQ-15-assisted-onboarding.md)
16. [REQ-16 — Caretaker Account & Multi-Patient Linking](REQ-16-caretaker-multi-patient-linking.md) *(deferred to Phase 2)*
17. [REQ-17 — Caretaker Review & Override](REQ-17-caretaker-review-and-override.md) *(deferred to Phase 2)*

## Decided since first draft

- **Persistence (REQ-00/REQ-08)**: the app persists everything it learns per user and per medicine (matched by chemical identity), across independent scans. A bill-only scan is remembered and later cross-verified/updated once a matching prescription is scanned, and vice versa — see the worked example in REQ-00.
- **Classification fallback (REQ-01)**: single combined scan flow (no mode picker); on ambiguous/low-confidence classification, the app asks the user to pick from the three known types (a lightweight tap, not data entry) — distinct from REQ-17's rule that no *structured data* is ever typed in on the Android device.
- **Brand/chemical mapping (REQ-02)**: local SQLite database as primary source, with an internet lookup fallback that gets cached back into the local database. Chemically-equivalent brands (e.g. Paracetamol/Calpol/Dolo) are matched by active component, and combination drugs require a full ingredient-set match (no partial overlap) — critical for linking a prescription to a differently-branded bill purchase in REQ-08.
- **Dosage wording & source (REQ-04/REQ-08)**: rewording confirmed. Dosage is targeted at elderly users specifically, sourced in order: caretaker-verified override (REQ-17, Phase 2) → patient's own prescription on file → local database → online lookup.
- **Translation (REQ-03)**: label text is extracted as English and translated into the user's chosen local language before narration (not just read as printed); narration uses a text-to-speech engine (specific engine deferred to Design).
- **Prescription legibility fallback (REQ-05)**: unreadable/unidentifiable entries fall back to cross-checking a bill on file, then are flagged pending caretaker review (REQ-17, Phase 2) — never on-device manual entry.
- **Reminder delivery (REQ-06)**: in-app only; schedule ends automatically at course completion or earlier if manually cancelled.
- **Shared course-completion source (REQ-06/REQ-08)**: refill reminders now stop at course completion the same way intake reminders do, sourced from the same prescription duration data (fixed days vs. indefinite/chronic) rather than tracking two separate end conditions. If the duration wasn't captured from the scan, both keep firing as usual and the gap is flagged for the caretaker to populate via REQ-17 (fixed days or indefinite).
- **Bill processing (REQ-07)**: multi-medicine bills produce one entry per medicine; unmatched medicines are flagged pending caretaker review (REQ-17, Phase 2) — never on-device manual entry.
- **AI chat model (REQ-09)**: leading candidate is Sarvam AI (Indian-language-first), evaluated ahead of generic Western LLMs, given local-language support is core to the app. Now explicitly Phase 3, not Phase 2 — see Phasing above.
- **New elderly-usability requirements added**: REQ-11 (simplified/large UI app-wide), REQ-12 (duplicate ingredient/interaction warning), REQ-13 (missed-dose escalation to a saved contact), REQ-14 (expiry date check), REQ-15 (assisted/caretaker-led onboarding).
- **App vs. web dashboard split (REQ-10/REQ-11/REQ-15/REQ-17)**: the elderly-facing app has exactly one, always-simplified UI — no denser mode, and critically, **no manual data-entry screen**, ever appears in it. All caretaker-facing complexity (ongoing medicine management, editing, resolving unclear scans, correcting dosage/reminders) lives only in the Phase 2 web dashboard (REQ-10/REQ-17). In the app itself, the caretaker's role during onboarding (REQ-15) is limited to account setup, scanning initial prescriptions/bills through the normal scan flow, and updating escalation contacts (REQ-13) at any time — never typing in medicine data, and never routinely adding medicines one by one.
- **Interaction check (REQ-12)**: same local DB + online fallback data source as REQ-02/REQ-04; runs automatically on every scan, no user action required.
- **Missed-dose escalation (REQ-13)**: threshold is per-medicine (not one fixed global value); channel is SMS in Phase 1 plus the REQ-10 web dashboard once it exists; caretaker sets up escalation contacts unilaterally during onboarding, no elderly-user approval required.
- **Expiry check (REQ-14)**: "nearing expiry" warning fires 1 week before the printed expiry date; an expired medicine never gets a dosage suggestion — the app tells the user to get a new one instead, no exceptions.
- **Caretaker account & multi-patient linking (REQ-16, Phase 2)**: the caretaker gets their own account, separate from any elderly user's, and one caretaker account can link to multiple elderly users (and vice versa — multiple caretakers per elderly user, e.g. siblings). Linking can happen optionally during REQ-15 onboarding or be added/changed later, initiated from the elderly side; access currently requires only a valid identifier, flagged as a known security gap to close in Phase 2.
- **Caretaker review & override (REQ-17, new, Phase 2)**: the Android app never asks anyone to manually enter structured data. Scans it can't confidently process are flagged pending review and resolved later via the Web UI, where the caretaker also has standing ability to review and correct the dosage/reminders currently shown to the elderly user — a human-supervision layer on top of the automatic pipeline.
- **Reason + audit trail required (REQ-17)**: every resolved scan and every dosage override requires the caretaker to state *why*, and is permanently recorded with which caretaker account acted and when — never a bare data edit.
- **Override vs. new prescription conflict (REQ-17)**: if a new prescription is scanned for a medicine that already has an active caretaker override, nothing changes automatically. The caretaker is explicitly asked to approve adopting the new prescription (which then supersedes the override) or decline it (override stays), and must give a reason either way.
- **Plain prescription updates always auto-apply (REQ-05)**: with no caretaker override in play, a newly scanned prescription always replaces the older one automatically — no confirmation, no conflict. This holds even if the existing entry only exists because a caretaker transcribed an illegible earlier scan (REQ-17's review queue) — that's not an override, so it never blocks a later prescription from taking over. The `DOSAGE_OVERRIDE` conflict above is the **only** case in the whole system that ever pauses for confirmation.

## Open questions for review

- REQ-03: the actual list of supported local languages is still undefined — planned for a later pass, needed before Design.
- Source of truth for "most common/elderly dosage" (which medical database/guideline) is not yet chosen — needed before Design phase.
- REQ-13: the actual per-medicine missed-dose thresholds need a data source — likely alongside the same reference data used for REQ-04/REQ-12.
- REQ-17: Phase 1 has no resolution path at all for a scan flagged pending review — it just waits until Phase 2 ships. Worth confirming this gap is acceptable for an initial release, or whether Phase 1 needs some interim fallback.
- **REQ-06 needs your confirmation**: you said an unacknowledged intake reminder repeats "every day till a bill is uploaded." That condition sounds like it belongs to the refill flow (REQ-08) rather than acknowledging a single dose was taken. Please confirm: should daily intake reminders just repeat until acknowledged, independent of any bill upload?
