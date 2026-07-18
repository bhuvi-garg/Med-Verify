# Unit: Dosage

Status: Draft — pending review

## Responsibility

Resolves a dosage suggestion per REQ-04's fallback order, limited to the tiers actually reachable in this slice.

## Location

`app/services/dosage_service.py` (public interface)
`app/repositories/dosage_reference_repository.py` (internal data access)

## Depends on

[MedicineResolution](../MedicineResolution/README.md) — for `get_chemical_component_ids()`. Reads the `chemical_dosage_reference` table.

**A note on personalization**: `chemical_dosage_reference` is deliberately **shared, non-personalized** data — Tier 3 in the algorithm below, the generic fallback used only when nothing more specific is known. Two accounts on the same medicine with genuinely different prescribed doses (e.g. `1-0-1` vs `2-0-2`) are **not** a problem for this table — that's exactly what Tier 2 (the patient's own `PRESCRIPTION_ITEM`, per-account via `USER_MEDICINE`) is for once REQ-05 exists, and Tier 2 outranks Tier 3. This is why `get_dosage` takes `user_medicine` below, not just `medicine` — even though Tiers 1–2 are no-ops in this slice, the interface needs to already be shaped to accept what it'll need, or every caller breaks the day Tier 2 is implemented.

## Constants

```python
STANDARD_DISCLAIMER: str = "General reference information, not personalized medical advice."
```

## Public interface

```python
@dataclass
class DosageResult:
    source: Literal["standard_reference", "unavailable"]
    amount: Optional[str]
    frequency: Optional[str]
    disclaimer: Optional[str]

def get_dosage(db: Session, user_medicine: UserMedicine, medicine: Medicine) -> DosageResult:
    """
    REQ-04's fallback order, tiers 1-2 unreachable in this slice (see below).
    `user_medicine` identifies WHICH ACCOUNT's dosage this is — required so
    that once Tier 1 (caretaker override) and Tier 2 (this account's own
    prescription) are implemented, this function can look up THIS account's
    data, not some other account's. `medicine` is passed separately (rather
    than derived from user_medicine.medicine) since the caller already has
    it in hand from the earlier medicine-resolution step, avoiding a
    redundant lookup.
    """
```

## Internal (repository-layer)

```python
def _find_by_chemical_component_id(db: Session, chemical_component_id: UUID) -> Optional[ChemicalDosageReference]: ...
```

## Algorithm

```
get_dosage(db, user_medicine, medicine):
    # Tier 1 (caretaker override) and Tier 2 (patient's own prescription) are
    # not implemented in this slice — no code path reaches them yet, since
    # REQ-17 overrides and REQ-05 prescriptions don't exist yet. Skipped entirely,
    # not "checked and found empty" — there's nowhere yet to check. When they
    # ARE implemented, both are looked up by user_medicine.id (or
    # user_medicine.account_id), which is exactly why this parameter exists
    # now rather than being added later as a breaking signature change:
    #   Tier 1: dosage_override_repository.find_active(db, user_medicine.id)
    #   Tier 2: prescription_item_repository.find_for(db, user_medicine.id)

    # Tier 3 (standard reference):
    1. components = medicine_resolution.get_chemical_component_ids(db, medicine.id)
    2. if len(components) != 1:
           return DosageResult("unavailable", None, None, None)
           # combination drugs (or zero-component data errors) are explicitly
           # unsupported in this slice
    3. component_id = the single id in components
    4. ref = dosage_reference_repository._find_by_chemical_component_id(db, component_id)
    5. if ref is None:
           return DosageResult("unavailable", None, None, None)
    6. return DosageResult("standard_reference", ref.elderly_dosage_amount,
                            ref.elderly_frequency, STANDARD_DISCLAIMER)

    # Tier 4 (online lookup): STUBBED — unreachable, same as
    # MedicineResolution's online fallback.
```

## Test cases

| # | Medicine's components | `chemical_dosage_reference` row exists for it? | Expected |
|---|---|---|---|
| 1 | 1 component | yes | `source="standard_reference"`, amount/frequency/disclaimer populated |
| 2 | 1 component | no | `source="unavailable"`, all else `None` |
| 3 | 2 components (combination) | yes (for one of them) | `source="unavailable"` regardless — combo drugs always unavailable in this slice |
| 4 | 0 components (data error) | n/a | `source="unavailable"` |
| 5 | Two different accounts' `user_medicine` rows, same underlying `medicine` (1 component), reference row exists | yes | **Both accounts get the identical `standard_reference` result** — this is correct *for this slice*, since Tier 2 (each account's own prescription) isn't implemented yet. This is the test that will need a new assertion once Tier 2 lands: the two accounts should then diverge based on their own `PRESCRIPTION_ITEM` data, not share Tier 3's generic value. |

## Open questions

- Combination-drug dosage lookup — same open item as `db-schema.md`. No design exists yet for how to combine or choose among multiple components' reference dosages.
- Source of truth for populating `chemical_dosage_reference` — still unresolved (REQ-04's original open question).
