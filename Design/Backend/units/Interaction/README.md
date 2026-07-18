# Unit: Interaction

Status: Draft — pending review

## Responsibility

Detects shared active ingredients across an account's medicines (REQ-12), scoped in this slice to same-account duplicate-ingredient detection only — a true drug-drug interaction database (two *different* ingredients that are dangerous together) is out of scope.

## Location

`app/services/interaction_service.py` (public interface)
`app/repositories/interaction_warning_repository.py` (internal data access)

## Depends on

[MedicineResolution](../MedicineResolution/README.md) — for `get_chemical_component_ids()` and `get_chemical_component_names()`. [UserMedicine](../UserMedicine/README.md) — for `list_for_account()`. Reads/writes the `interaction_warning` table.

## Public interface

```python
@dataclass
class InteractionWarningView:
    warning_type: str
    detail: str

def check_duplicate_ingredients(
    db: Session, account_id: UUID, user_medicine: UserMedicine
) -> list[InteractionWarningView]:
    """
    Compares user_medicine's chemical components against every OTHER
    user_medicine row for the same account. Any shared component produces
    a warning, persisted and returned. Uses ANY overlap, not exact-set
    match — see Notes.
    """
```

## Internal (repository-layer)

```python
def _create(db: Session, user_medicine_id: UUID, warning_type: str, detail: str) -> InteractionWarning: ...
```

## Algorithm

```
check_duplicate_ingredients(db, account_id, user_medicine):
    1. target_components = medicine_resolution.get_chemical_component_ids(db, user_medicine.medicine_id)
    2. others = [row for row in user_medicine.list_for_account(db, account_id)
                 if row.id != user_medicine.id]
    3. warnings = []
    4. for other in others:
           other_components = medicine_resolution.get_chemical_component_ids(db, other.medicine_id)
           shared = target_components & other_components
           if shared:
               shared_names = medicine_resolution.get_chemical_component_names(db, shared)
               detail = f"Also contains {', '.join(shared_names)}, same as '{other.medicine.brand_name}' already on file."
               interaction_warning_repository._create(db, user_medicine.id, "duplicate_ingredient", detail)
               warnings.append(InteractionWarningView("duplicate_ingredient", detail))
    5. return warnings
```

## Test cases

| # | Other `user_medicine` rows for account | Expected |
|---|---|---|
| 1 | none | `[]` |
| 2 | one, no shared ingredients | `[]` |
| 3 | one, shares 1 of 1 ingredient | 1 warning; `detail` names the shared ingredient and the other brand |
| 4 | two others, each sharing a (possibly different) ingredient | 2 warnings, one per other medicine |
| 5 | one other, shares 1 of 2 ingredients (combo drug, partial overlap) | 1 warning — **partial overlap DOES warn here** |
| 6 | `user_medicine` itself appears in the account's list (must be excluded) | it is never compared against itself; 0 warnings from self-comparison |

## Notes

- Test case 5 is the important contrast with [UserMedicine](../UserMedicine/README.md): that unit requires an *exact* set match to treat two scans as "the same medicine," while this unit warns on *any* shared ingredient. These aren't inconsistent — merging identity needs certainty (to avoid conflating two genuinely different medicines), while flagging a double-dosing risk should err on the side of over-warning.

## Deferred

- A real drug-drug interaction database (two different, non-overlapping ingredients that are still dangerous together) is not implemented. This unit only ever produces `warning_type="duplicate_ingredient"`.
