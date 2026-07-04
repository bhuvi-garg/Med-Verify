# REQ-04 — Common Dosage Suggestion

Status: Approved

## Description

When a medicine has been identified (REQ-02), the system suggests a dosage for that medicine, based on its chemical identity. The app targets elderly users, so the dosage suggested is the elderly-appropriate dosage, not a general single-adult-standard figure.

## Wording (confirmed)

The initial notes described this as suggesting the dosage that "gives 100 percent result." Confirmed reworded to:

> "Suggest the standard/most commonly prescribed dosage for the identified medicine, sourced from an authoritative drug reference, along with a clear disclaimer that this is general information and not a personalized medical recommendation."

## Dosage source order (decided)

When suggesting a dosage for an identified medicine, the system checks sources in this order and uses the first one available:

1. A caretaker-verified override for this medicine, if one has been set from the Web UI ([REQ-17](REQ-17-caretaker-review-and-override.md), Phase 2) — a human has already checked this, so it outranks even the patient's own prescription.
2. A prescription already on file for this medicine, by chemical identity (see [REQ-00](REQ-00-behavior-model.md) persistence model) — i.e. if this patient was already prescribed this medicine, use their actual prescribed dosage rather than a generic figure.
3. The local database's standard elderly dosage reference.
4. An online lookup, if not available locally.

## Acceptance criteria

- Given a medicine's chemical identity, the system returns a dosage using the source order above, and indicates which source it came from (caretaker override vs. prior prescription vs. standard reference vs. online lookup).
- The dosage reflects elderly-appropriate dosing, not general adult dosing.
- When the suggestion is not sourced from a caretaker override or the patient's own prescription, it is displayed with a disclaimer that it is general reference information, not a prescription or personalized medical advice.

## Open questions

- Which specific reference source/guideline populates the local database's elderly dosage data? To be decided before Design.
