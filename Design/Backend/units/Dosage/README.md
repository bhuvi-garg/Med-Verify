# Unit: Dosage

Status: Draft — pending review

## Responsibility

Resolves a dosage suggestion per REQ-04's fallback order, limited to the tiers actually reachable in this slice.

## Location

`app/services/dosage_service.py` (public interface)
`app/repositories/dosage_reference_repository.py` (internal data access)

## Depends on

[MedicineResolution](../MedicineResolution/README.md) — for `get_chemical_component_ids()`. Reads the `chemical_dosage_reference` table.

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

def get_dosage(db: Session, medicine: Medicine) -> DosageResult:
    """
    REQ-04's fallback order, tiers 1-2 unreachable in this slice (see below).
    """
```

## Internal (repository-layer)

```python
def _find_by_chemical_component_id(db: Session, chemical_component_id: UUID) -> Optional[ChemicalDosageReference]: ...
```

## Algorithm

```
get_dosage(db, medicine):
    # Tier 1 (caretaker override) and Tier 2 (patient's own prescription) are
    # not implemented in this slice — no code path reaches them yet, since
    # REQ-17 overrides and REQ-05 prescriptions don't exist yet. Skipped entirely,
    # not "checked and found empty" — there's nowhere yet to check.

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

## Open questions

- Combination-drug dosage lookup — same open item as `db-schema.md`. No design exists yet for how to combine or choose among multiple components' reference dosages.
- Source of truth for populating `chemical_dosage_reference` — still unresolved (REQ-04's original open question).
