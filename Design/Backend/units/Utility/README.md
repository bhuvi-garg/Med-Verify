# Unit: Utility

Status: Draft — pending review

## Responsibility

Small, feature-agnostic helper functions that any other unit may call. Nothing here knows about medicines, scans, or any domain concept — if a function needs domain knowledge, it belongs in a feature unit instead.

## Location

`app/utils/`

## Depends on

Nothing else in this system (leaf unit). May depend on the standard library and the DB session for `store_image`'s file-write, but not on any other unit.

## Public interface

```python
# app/utils/text.py
def normalize_text(value: str) -> str:
    """
    Trims leading/trailing whitespace and casefolds (Unicode-aware
    lowercasing) the input. Used for case/whitespace-insensitive
    comparisons (e.g. brand name lookups).
    """
```

```python
# app/utils/clock.py
def get_today() -> date:
    """
    Returns date.today(). Exists as its own function — not called
    directly as date.today() elsewhere — purely so tests can monkeypatch
    this one function to control "what day it is" without needing to
    freeze real system time.
    """
```

```python
# app/utils/image_storage.py
def store_image(image_bytes: bytes) -> str:
    """
    Saves image_bytes to local disk under a configured uploads directory
    (path from settings/config, not hardcoded), named by a random UUID
    + '.jpg'. Returns the relative path as a string.
    Raises: IOError if the write fails.
    """
```

## Test cases

### `normalize_text`
| # | Input | Expected |
|---|---|---|
| 1 | `"Calpol"` | `"calpol"` |
| 2 | `"  Calpol  "` | `"calpol"` |
| 3 | `"CALPOL"` | `"calpol"` |
| 4 | `""` | `""` |
| 5 | `"Café"` (non-ASCII) | `"café"` — casefold, not just `.lower()`, handles this correctly |

### `get_today`
| # | Scenario | Expected |
|---|---|---|
| 1 | called normally | returns `date.today()` |
| 2 | monkeypatched in a test (e.g. `monkeypatch.setattr(clock, "get_today", lambda: date(2026, 1, 1))`) | callers observe the patched date, proving no other unit calls `date.today()` directly |

### `store_image`
| # | Input | Expected |
|---|---|---|
| 1 | valid bytes | returns a non-empty path string; file exists on disk with that exact content |
| 2 | empty bytes (`b""`) | still writes and returns a path — validating non-emptiness is the `API` unit's job, not this one's |
| 3 | uploads directory doesn't exist yet | creates it (or raises a clear `IOError` — pick one behavior and test it; recommend auto-create, since that's the common startup-friendly choice) |

## Notes

- Every unit that currently compares text (`MedicineResolution`) or reads "today" (`Expiry`, `Scan`) must go through this unit's functions, not inline `.strip().lower()` or `date.today()` calls — that's what makes those units' tests deterministic.
