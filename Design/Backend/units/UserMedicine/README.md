# Unit: UserMedicine

Status: Draft — pending review

## Responsibility

Resolves or creates the per-account `user_medicine` record (REQ-00's persistence anchor), applying REQ-02's chemical-identity equivalence rule: two medicines are the same for this account only if their **full** set of chemical components matches exactly (no partial-overlap merging).

## Location

`app/services/user_medicine_service.py` (public interface)
`app/repositories/user_medicine_repository.py` (internal data access)

## Depends on

[MedicineResolution](../MedicineResolution/README.md) — for `get_chemical_component_ids()`. Reads/writes the `user_medicine` table (see [db-schema.md](../../db-schema.md)).

## Public interface

```python
def get_or_create_user_medicine(
    db: Session, account_id: UUID, medicine: Medicine, printed_expiry_date: Optional[date]
) -> tuple[UserMedicine, bool]:
    """
    Returns (user_medicine, was_created). If an existing user_medicine for
    this account has the exact same chemical component set as `medicine`,
    reuses it (and updates its printed_expiry_date). Otherwise creates a
    new row.
    """

def list_for_account(db: Session, account_id: UUID) -> list[UserMedicine]:
    """All user_medicine rows for this account, each with .medicine eagerly loaded."""
```

## Internal (repository-layer)

```python
def _create(db: Session, account_id: UUID, medicine_id: UUID, origin_scan_type: str,
            printed_expiry_date: Optional[date]) -> UserMedicine: ...

def _update_expiry(db: Session, user_medicine_id: UUID, printed_expiry_date: Optional[date]) -> None: ...
```

## Algorithm

```
get_or_create_user_medicine(db, account_id, medicine, printed_expiry_date):
    1. target_components = medicine_resolution.get_chemical_component_ids(db, medicine.id)
    2. existing_rows = list_for_account(db, account_id)
    3. for row in existing_rows:
           row_components = medicine_resolution.get_chemical_component_ids(db, row.medicine_id)
           if row_components == target_components:   # exact set equality —
                                                       # partial overlap does NOT match
               user_medicine_repository._update_expiry(db, row.id, printed_expiry_date)
               return (row, False)
    4. new_row = user_medicine_repository._create(
           db, account_id, medicine.id, origin_scan_type="medicine", printed_expiry_date
       )
       return (new_row, True)
```

## Test cases

### `get_or_create_user_medicine`
| # | Existing `user_medicine` rows for account | New scan | Expected |
|---|---|---|---|
| 1 | none | Calpol (Paracetamol) | creates new row, `was_created=True` |
| 2 | Calpol (Paracetamol) already on file | Calpol again | reuses same row, `was_created=False`, expiry updated |
| 3 | Calpol (Paracetamol) already on file | Dolo (also just Paracetamol) | reuses same row — brand differs, chemical identity matches |
| 4 | Calpol (Paracetamol) already on file | Ibuprofen (different ingredient) | creates new row |
| 5 | Combo drug "X" (Paracetamol + Caffeine) already on file | Calpol (Paracetamol only) | creates new row — **partial overlap is not a match** |
| 6 | Combo drug "X" (Paracetamol + Caffeine) already on file | Combo drug "X" again | reuses same row |
| 7 | Calpol (Paracetamol) already on file, `printed_expiry_date=2026-01-01` | Calpol again, `printed_expiry_date=2027-06-01` | reused row's `printed_expiry_date` is now `2027-06-01`, not the old value |

### `list_for_account`
| # | Setup | Expected |
|---|---|---|
| 1 | account has no rows | `[]` |
| 2 | account has 3 rows | all 3 returned, each with `.medicine` populated (not a separate query needed by the caller) |
| 3 | a *different* account has rows too | only the requested account's rows are returned |

## Notes

- Test case 5 above is the one to keep in mind whenever this unit's logic is touched — it's the whole point of chemical-identity equivalence being an *exact set match*, not "any overlap." (Contrast with [Interaction](../Interaction/README.md), which deliberately uses the opposite rule — *any* overlap is enough to warn — because merging identity and flagging risk are different concerns with different safety implications.)
