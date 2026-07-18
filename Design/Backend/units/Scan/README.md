# Unit: Scan

Status: Draft — pending review

## Responsibility

The `/scan` use case end to end, for the medicine path, **starting from what Android already extracted** — this unit does not perform OCR or classification itself; those run on-device (see [Design/Android/units/](../../../Android/units/README.md)) and their result is the input here. Calls every backend feature unit in the right order and assembles the final result.

## Location

`app/services/scan_service.py`

## Depends on

[MedicineResolution](../MedicineResolution/README.md), [UserMedicine](../UserMedicine/README.md), [Expiry](../Expiry/README.md), [Interaction](../Interaction/README.md), [Dosage](../Dosage/README.md), [Utility](../Utility/README.md) — for `store_image()`. Writes the `scan_artifact` table directly (no dedicated feature unit owns it, since "log this scan" isn't really a feature of its own).

**Does not** depend on `OCR` or `Classification` — those aren't backend units at all; they run on Android and their output arrives as part of this unit's input.

## Public interface

```python
@dataclass
class ScanInput:
    account_id: UUID
    image_bytes: bytes                              # ALWAYS present — every scan uploads its image
    client_scan_type: Literal["medicine", "unknown"] # Android's own classification result
    detected_brand_name: Optional[str]               # from Android's on-device OCR
    detected_expiry_date: Optional[date]              # from Android's on-device OCR
    ocr_raw_text: Optional[str]                       # from Android's on-device OCR, for audit

@dataclass
class ResolvedScanResult:
    status: Literal["resolved"]
    scan_type: Literal["medicine"]
    user_medicine_id: UUID
    medicine_brand_name: str
    chemical_components: list[str]
    expired: bool
    expiring_soon: bool
    printed_expiry_date: Optional[date]
    interaction_warnings: list[InteractionWarningView]
    dosage: Optional[DosageResult]   # None only when expired

@dataclass
class PendingScanResult:
    status: Literal["pending"]
    scan_artifact_id: UUID
    message: str

class ScanService:
    def __init__(self, db: Session):
        self.db = db

    def process_scan(self, scan_input: ScanInput) -> ResolvedScanResult | PendingScanResult:
        ...
```

## Internal (repository-layer, owned directly by this unit)

```python
def _create_scan_artifact(
    db: Session, account_id: UUID, image_ref: str, ocr_raw_text: Optional[str],
    scan_type_guess: Optional[str], status: Literal["resolved", "pending"],
    resolved_into_user_medicine_id: Optional[UUID] = None,
) -> ScanArtifact:
    """Called exactly once per process_scan() call, regardless of outcome —
    every scan gets logged, image included."""
```

## Algorithm

```
process_scan(scan_input):
    1. image_ref = utility.store_image(scan_input.image_bytes)
       # ALWAYS stored, before anything else — this is what makes the
       # image available to the caretaker later no matter what happens next.

    2. if scan_input.client_scan_type == "unknown":
           artifact = self._create_scan_artifact(
               scan_input.account_id, image_ref, scan_input.ocr_raw_text,
               scan_type_guess=None, status="pending")
           return PendingScanResult(
               "pending", artifact.id,
               "We couldn't confidently read this. It's been saved for review.")

    3. medicine = medicine_resolution.resolve_medicine(self.db, scan_input.detected_brand_name)
       if medicine is None:
           artifact = self._create_scan_artifact(
               scan_input.account_id, image_ref, scan_input.ocr_raw_text,
               scan_type_guess="medicine", status="pending")
           return PendingScanResult(
               "pending", artifact.id,
               "We couldn't confidently read this. It's been saved for review.")

    4. user_medicine, _created = user_medicine.get_or_create_user_medicine(
           self.db, scan_input.account_id, medicine, scan_input.detected_expiry_date)

    5. expiry = expiry.check_expiry(scan_input.detected_expiry_date)

    6. warnings = interaction.check_duplicate_ingredients(self.db, scan_input.account_id, user_medicine)
       # runs regardless of expiry — independent concern, must not be skipped

    7. if expiry.expired:
           dosage = None   # REQ-14: no exceptions, ever
       else:
           dosage = dosage.get_dosage(self.db, medicine)

    8. component_ids = medicine_resolution.get_chemical_component_ids(self.db, medicine.id)
       component_names = medicine_resolution.get_chemical_component_names(self.db, component_ids)

    9. self._create_scan_artifact(
           scan_input.account_id, image_ref, scan_input.ocr_raw_text,
           scan_type_guess="medicine", status="resolved",
           resolved_into_user_medicine_id=user_medicine.id)
       # written even on success — every scan is logged, not just failures

    10. return ResolvedScanResult(
           status="resolved", scan_type="medicine",
           user_medicine_id=user_medicine.id,
           medicine_brand_name=medicine.brand_name,
           chemical_components=component_names,
           expired=expiry.expired, expiring_soon=expiry.expiring_soon,
           printed_expiry_date=scan_input.detected_expiry_date,
           interaction_warnings=warnings,
           dosage=dosage,
       )
```

## Test cases

Constructing `ScanInput` directly with different field combinations (no OCR adapter involved — that's entirely upstream, on Android):

| # | Scenario | `ScanInput` fields | Expected |
|---|---|---|---|
| 1 | Happy path, single-ingredient, dosage available | `client_scan_type="medicine"`, `detected_brand_name="Calpol"`, expiry far future | `ResolvedScanResult`, `expired=False`, dosage populated; a `scan_artifact` row exists with `status="resolved"` |
| 2 | Android couldn't classify | `client_scan_type="unknown"` | `PendingScanResult`; `scan_artifact` row has `status="pending"`, `scan_type_guess=None` |
| 3 | Android confident "medicine" but unrecognized brand | `client_scan_type="medicine"`, `detected_brand_name="Not A Real Brand"` | `PendingScanResult`; `scan_artifact` row has `status="pending"`, `scan_type_guess="medicine"` |
| 4 | Expired medicine | expiry in the past | `ResolvedScanResult`, `expired=True`, `dosage=None` (never computed — verify `Dosage.get_dosage` was not called) |
| 5 | Nearing expiry | expiry 3 days out | `ResolvedScanResult`, `expiring_soon=True`, dosage still populated |
| 6 | Shared ingredient, different medicine | account already has "Combo-X" (Paracetamol + Caffeine); this scan is "Calpol" (Paracetamol only) | `ResolvedScanResult` with a **new** `user_medicine_id` (different component sets), `interaction_warnings` has 1 entry naming Combo-X |
| 7 | Same medicine scanned twice | first call creates a `user_medicine`; second call is the same brand | second call returns the **same** `user_medicine_id` as the first |
| 8 | Combination drug | 2 chemical components, expiry fine | `ResolvedScanResult`, `dosage.source == "unavailable"` |
| 9 | **Every case above, including 2 and 3** | — | `image_ref` on the resulting `scan_artifact` row is always non-null and points at real stored image bytes — this is the invariant that changed in this pass: images are logged for every outcome, not just pending ones |

## Notes

- Step 6 (interaction check) intentionally runs even when the medicine is expired — expiry only suppresses the *dosage* suggestion (step 7), not the interaction warning.
- Step 1 always runs first, before any classification/resolution branching — this guarantees the image is captured even if something later in the pipeline fails unexpectedly.
