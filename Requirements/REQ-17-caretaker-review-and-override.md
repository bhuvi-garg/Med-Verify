# REQ-17 — Caretaker Review & Override

Status: Approved — **deferred to Phase 2, not in initial scope**

## Description

The Android app never asks anyone to manually type in structured data — not the elderly user, and not a caretaker operating the device (including during [REQ-15](REQ-15-assisted-onboarding.md) onboarding). When a scan can't be confidently processed, that responsibility moves entirely to the caretaker's Web UI ([REQ-10](REQ-10-caretaker-web-dashboard.md)), where the caretaker looks at the original captured image and types out the correct details themselves.

Beyond resolving failed scans, the caretaker can also review and correct the dosage and reminders the elderly user is currently being shown — a human supervision layer on top of the automatic pipeline, so a system mistake doesn't silently become the elderly user's understanding of their own medication.

## Why this replaces on-device manual entry

Manual entry of structured medical data (a dosage, a frequency, a quantity) is exactly the kind of complexity [REQ-11](REQ-11-simplified-ui-mode.md) says the elderly-facing app must never expose, and it was already awkward to justify even as a caretaker-only fallback during onboarding. Now that a caretaker Web UI exists (REQ-10), there's a better home for it: the caretaker reviews the original image on a proper screen, at their own pace, not hunched over the elderly user's phone.

## What happens instead, when a scan can't be resolved

Per [REQ-05](REQ-05-prescription-dosage-extraction.md)'s and [REQ-07](REQ-07-pharmacy-bill-processing.md)'s fallback chains, an unresolved line item (from a prescription or a bill) is flagged **pending caretaker review** and stored along with the original scanned image. Per [REQ-00](REQ-00-behavior-model.md)'s silent-skip principle, no dosage suggestion or reminder is created for it in the meantime — it simply waits.

- **In Phase 1** (before this dashboard exists), a pending item has no resolution path yet; it stays pending until Phase 2 ships.
- **In Phase 2**, the caretaker sees a review queue in the Web UI, opens each pending item, views the original image, and types in the correct medicine/dosage/quantity. Once entered, it flows through the same downstream logic (REQ-04/REQ-06/REQ-08) as if it had been read successfully.

## Caretaker override of dosage/reminders

Independent of the review queue, the caretaker can open any medicine currently tracked for their linked elderly user and view exactly what dosage and reminder schedule the app is using for it. They can correct it directly. A caretaker-entered value is treated as **verified** and takes priority over every automatic dosage source — see the updated source order in [REQ-04](REQ-04-dosage-suggestion.md) and [REQ-08](REQ-08-refill-reminder.md), where it now ranks above even the patient's own scanned prescription.

## Every correction requires a reason, and is audited (decided)

Neither resolving a pending scan nor setting a dosage override is a bare data-entry action. Each time, the caretaker must also state **why** (e.g. "confirmed against Dr. X's prescription dated ...", "pharmacist advised this dose"), and the system records **which caretaker account** made the change and **when**, in a permanent, timestamped audit trail. This is non-negotiable given this is medical data feeding directly into what an elderly patient believes about their own medication — the "why" is what makes the resolved/overridden data trustworthy downstream, not just present.

## Conflicting prescription vs. an existing override — and *only* that (decided)

A newly scanned prescription **always** wins over an older one automatically, with no confirmation and no conflict — see REQ-05. That includes the case where the existing entry only exists because a caretaker helped resolve an unreadable scan (the review-queue flow above): filling in what an illegible prescription said is not an override, and doesn't block a later prescription from taking over normally.

The **only** case that needs explicit caretaker confirmation is when a medicine already has an actual `DOSAGE_OVERRIDE` in effect — a caretaker's deliberate correction, not just a transcription — and a new prescription is scanned for it. Only then does the system pause instead of auto-applying the new prescription:

1. The system raises it to the caretaker as a decision: keep the current override, or adopt the new prescription's dosage.
2. **If the caretaker approves adopting the new prescription**: the override is superseded, and the dosage now flows from the newly scanned prescription going forward (REQ-04's normal source order resumes, since no override is active anymore).
3. **If the caretaker declines**: the override remains active and unchanged, but the caretaker must still provide a reason for keeping it over the newer prescription.
4. Either way, the decision — approved or declined, who decided, when, and why — is timestamped and stored permanently. Nothing about this conflict is ever resolved silently.

## Acceptance criteria

- The Android app has no manual-data-entry screen anywhere, including during REQ-15 onboarding — every fallback chain that used to end in on-device manual entry now ends in "flagged pending caretaker review" instead.
- The Web UI shows a queue of pending-review items per linked elderly user, each showing the original scanned image, letting the caretaker enter the correct structured data along with a reason.
- Once a pending item is resolved via the Web UI, it feeds into the same dosage/reminder logic as a successful scan would have.
- The Web UI lets the caretaker view and edit the dosage/reminder currently active for any of their linked elderly user's medicines, requiring a reason for the edit.
- A caretaker-entered/verified dosage takes precedence over prescription-derived, standard-reference, or online-lookup dosages for that medicine, until superseded (see conflict handling above).
- Every resolution, override, and conflict decision is recorded with: the acting caretaker's account, a timestamp, and the caretaker's stated reason — permanently, not just the latest state.
- When a new prescription scan conflicts with an existing override, the caretaker is explicitly asked to approve or decline adopting it, with a reason required either way; nothing is switched or kept automatically without that decision.

## Open questions

- None outstanding on reason-tracking or conflict handling — see the decisions above. Remaining open items are tracked in [ARCH-03](../Arch/ARCH-03-data-model.md) (schema specifics) and [ARCH-06](../Arch/ARCH-06-scan-combination-behavior.md) (interaction with the scan-combination matrix).
