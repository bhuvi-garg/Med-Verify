# Unit: MedicineResolution

Status: Draft — pending review

## Responsibility

Resolves a brand name (from OCR) to a `medicine` reference-data record, per REQ-02's "local database, then online fallback" lookup order. Also exposes chemical-component lookups that other units need.

## Location

`app/services/medicine_resolution_service.py` (public interface)
`app/repositories/medicine_repository.py` (internal data access, not called by other units directly)

## Depends on

[Utility](../Utility/README.md) — for `normalize_text()`. Reads the `medicine`, `chemical_component`, and `medicine_component` tables (see [db-schema.md](../../db-schema.md)).

## Public interface

```python
def resolve_medicine(db: Session, brand_name: str) -> Optional[Medicine]:
    """
    Resolves a brand name to a medicine record, local-first with an
    (currently stubbed) online fallback. Returns None if unresolved.
    """

def get_chemical_component_ids(db: Session, medicine_id: UUID) -> set[UUID]:
    """The full set of chemical_component_id for a medicine. Empty set if none linked."""

def get_chemical_component_names(db: Session, component_ids: set[UUID]) -> list[str]:
    """Names for a set of component IDs, sorted alphabetically (for deterministic API responses)."""
```

## Internal (repository-layer, not part of the public interface)

```python
def _find_by_brand_name(db: Session, normalized_brand_name: str) -> Optional[Medicine]:
    """Exact match against a similarly-normalized medicine.brand_name column."""
```

## Algorithm

```
resolve_medicine(db, brand_name):
    1. normalized = utility.normalize_text(brand_name)
    2. local = medicine_repository._find_by_brand_name(db, normalized)
    3. if local is not None: return local
    4. [STUBBED] online lookup — always treated as not found in this slice.
       Real implementation, later: call an online adapter; if found, insert
       a new `medicine` row with source='online' before returning it, per
       REQ-02's "cache online results back into the local database" rule.
    5. return None
```

```
get_chemical_component_ids(db, medicine_id):
    1. query medicine_component where medicine_id = medicine_id
    2. return the set of chemical_component_id values (empty set if none)
```

## Test cases

### `resolve_medicine`
| # | DB contains | Query | Expected |
|---|---|---|---|
| 1 | `brand_name="Calpol"` | `"Calpol"` | matching row |
| 2 | `brand_name="Calpol"` | `"  calpol  "` | matching row (normalized match) |
| 3 | `brand_name="Calpol"` | `"CALPOL"` | matching row (case-insensitive) |
| 4 | `brand_name="Calpol"` | `"Dolo"` | `None` |
| 5 | (empty table) | `"Calpol"` | `None` |

### `get_chemical_component_ids`
| # | Setup | Expected |
|---|---|---|
| 1 | medicine linked to 1 component | set with 1 UUID |
| 2 | medicine linked to 2 components (combination drug) | set with 2 UUIDs |
| 3 | medicine linked to 0 components | empty set |

### `get_chemical_component_names`
| # | Input IDs | Expected |
|---|---|---|
| 1 | IDs for "Caffeine" and "Paracetamol" (inserted in that order) | `["Caffeine", "Paracetamol"]` — alphabetical, not insertion order |

## Open questions

- Fuzzy/partial brand-name matching — exact-normalized-match-only will miss OCR noise (extra whitespace already handled, but not misreadings like "Ca1pol"). Revisit once real OCR output exists to test against.
- Online fallback (step 4) is unreachable/untested until a real online lookup adapter exists.
