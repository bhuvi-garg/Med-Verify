# Unit: Expiry

Status: Draft — pending review

## Responsibility

Decides expired / expiring-soon / fine, per REQ-14.

## Location

`app/services/expiry_service.py`

## Depends on

[Utility](../Utility/README.md) — for `get_today()`. No database access.

## Constants

```python
EXPIRING_SOON_WINDOW_DAYS: int = 7
```

## Public interface

```python
@dataclass
class ExpiryResult:
    expired: bool
    expiring_soon: bool

def check_expiry(printed_expiry_date: Optional[date]) -> ExpiryResult:
    """Uses Utility.get_today() internally — caller does not pass today's date in."""
```

## Algorithm

```
1. today = utility.get_today()
2. if printed_expiry_date is None:
       return ExpiryResult(expired=False, expiring_soon=False)
       # can't judge what we don't know — REQ-00 silent-skip, not a false "safe"
3. if printed_expiry_date < today:
       return ExpiryResult(expired=True, expiring_soon=False)
4. if printed_expiry_date <= today + timedelta(days=EXPIRING_SOON_WINDOW_DAYS):
       return ExpiryResult(expired=False, expiring_soon=True)
5. return ExpiryResult(expired=False, expiring_soon=False)
```

## Test cases

Assume `get_today()` is monkeypatched to return `2026-07-18` for all rows below.

| # | `printed_expiry_date` | Expected |
|---|---|---|
| 1 | `None` | `(False, False)` |
| 2 | `2026-06-01` (in the past) | `(True, False)` |
| 3 | `2026-07-18` (today, boundary) | `(False, False)` — the expiry date itself is still valid, not yet expired |
| 4 | `2026-07-17` (yesterday) | `(True, False)` |
| 5 | `2026-07-25` (exactly 7 days out, boundary) | `(False, True)` |
| 6 | `2026-07-26` (8 days out) | `(False, False)` |
| 7 | `2026-12-01` (far future) | `(False, False)` |

## Notes

- This unit never calls `date.today()` directly — always through `Utility.get_today()` — which is exactly what makes the boundary test cases above deterministic and repeatable.
