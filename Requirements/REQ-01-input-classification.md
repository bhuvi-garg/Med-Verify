# REQ-01 — Input Classification

Status: Draft — pending review

## Description

When the user scans an item using the app, the system must first identify what kind of input it is, so it knows which downstream requirements apply. Each scan is exactly one of three independent input types — there is no combined input:

- A **medicine** (pill, strip, bottle, or packaging)
- A **prescription** (doctor's handwritten or printed note)
- A **pharmacy bill** (a purchase receipt)

See [REQ-00](REQ-00-behavior-model.md) — the app does not require or assume any two of these are scanned together; each is handled as its own independent flow.

## Acceptance criteria

- There is a single, combined scan flow — the user does not pick a mode before scanning; the app determines the type automatically.
- Given a scanned image/photo, the system classifies it as exactly one of: medicine, prescription, or pharmacy bill.
- The correct downstream flow is triggered based on classification: REQ-02–REQ-04 for medicine, REQ-05–REQ-06 for prescription, REQ-07–REQ-08 for pharmacy bill.
- If classification is ambiguous or confidence is low, the app falls back to asking the user to manually select the type, rather than guessing or failing.

## Open questions

- What confidence threshold triggers the manual-selection fallback? To be tuned during Design/testing.
