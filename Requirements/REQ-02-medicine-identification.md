# REQ-02 — Medicine Identification

Status: Draft — pending review

## Description

When the scanned input is identified as a medicine (REQ-01), the system must determine the medicine's chemical/generic name (not just its brand name), and use that to look up authoritative information about it.

## Data source (decided)

Brand name → chemical name resolution uses a local SQLite database as the primary source of truth, with a fallback to an internet lookup when the medicine isn't found locally. (Whether/how internet results get written back into the local database is a Design decision.)

## Chemical-equivalence matching (critical, decided)

The system stores **all active chemical components** for every medicine, not just a single name, and treats two products as the same medicine whenever their chemical components match — regardless of brand. For example, Paracetamol, Calpol, and Dolo are different brand names for the same active ingredient and must be recognized as equivalent.

This is critical beyond just identification: [REQ-08](REQ-08-refill-reminder.md)'s refill reminder depends on it. A patient may be prescribed "Paracetamol" but buy "Calpol" at the pharmacy — the app must recognize these as the same medicine to correctly link the prescription's dosage to the bill's purchase for refill calculations (see [REQ-00](REQ-00-behavior-model.md)).

## Acceptance criteria

- Given a scanned medicine, the system resolves the brand name printed on it to its full set of chemical/active-ingredient component(s), using the local database first, then an internet lookup if not found locally.
- Two medicines are treated as the same for cross-referencing purposes (prescription ↔ bill ↔ scanned medicine) only when their full set of active chemical components matches exactly — a combination drug matches another product only if every ingredient matches; partial/majority overlap does not count as equivalent.
- When an internet lookup resolves a medicine not present in the local database, the result is added to the local database so future scans of that medicine resolve locally.
- The resolved chemical identity is used to query dosage information, feeding into REQ-04 (dosage suggestion).
