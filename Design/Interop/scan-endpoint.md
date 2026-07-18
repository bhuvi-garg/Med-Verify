# Interop Contract — `POST /scan` (Medicine)

Status: Draft — pending review

## Scope

The request/response JSON contract for the medicine-scan vertical slice, implemented by the [API unit](../Backend/units/API/README.md) and backed by the [Scan unit](../Backend/units/Scan/README.md). This is what both the Android app and the Backend build against — a change to the shape below should happen here first, not be discovered as a mismatch later.

## Authentication (placeholder for this slice)

REQ-15 onboarding and account auth aren't implemented yet, so this slice passes `account_id` explicitly in the request body rather than deriving it from a session. **This is temporary** — once auth exists, `account_id` will come from the authenticated session instead, and this field will be removed from the request body. Flagging this now so it isn't mistaken for the final design.

## Request

`POST /scan`, `multipart/form-data`. **OCR and classification run on-device in the Android app** ([Design/Android/units/](../Android/units/README.md)) — the backend does not perform OCR itself. The request therefore carries both the image (always, for the caretaker's later review — see [ARCH-00](../../Arch/ARCH-00-overview.md#why-not-put-logic-on-device-and-the-one-exception)) and whatever Android's on-device OCR/classification already extracted:

| Field | Type | Notes |
|---|---|---|
| `account_id` | string (UUID) | Placeholder until real auth exists — see above. |
| `image` | file | The captured photo. **Always included, on every scan, regardless of outcome.** Max **10 MB**. Accepted content types: `image/jpeg`, `image/png`. Anything else (or over the size limit) is a request error, not a "pending" result — see Request errors below. |
| `client_scan_type` | string: `"medicine"` \| `"unknown"` | Android's own classification result (REQ-01). `"unknown"` means Android's on-device OCR/classification wasn't confident — see [Android's Classification unit](../Android/units/Classification/README.md). |
| `detected_brand_name` | string, optional | From Android's on-device OCR. Omitted/null if OCR didn't extract one. |
| `detected_expiry_date` | string (ISO date), optional | From Android's on-device OCR. Omitted/null if OCR didn't extract one. |
| `ocr_raw_text` | string, optional | The raw text Android's OCR extracted, regardless of whether structured fields could be parsed from it. Stored for a future caretaker reviewer's context, not used in backend logic. |

10 MB comfortably covers a typical phone photo without letting a pathological upload tie up the server; JPEG/PNG covers what a phone camera or gallery picker will produce. All of the above are enforced/read by the `API` unit before anything reaches `Scan`.

## Response — always `200 OK`

HTTP status codes are reserved for actual request-level errors (bad input, server failure) — a "pending" or "expired" outcome is a normal business result, not an HTTP error, per REQ-00's "not an error state" principle. The `status` field in the body is what callers branch on.

### `status: "resolved"` — confidently identified, not expired

```json
{
  "status": "resolved",
  "scan_type": "medicine",
  "user_medicine_id": "6f9c...",
  "medicine": {
    "brand_name": "Calpol",
    "chemical_components": ["Paracetamol"]
  },
  "expiry": {
    "expired": false,
    "expiring_soon": false,
    "printed_expiry_date": "2027-03-01"
  },
  "interaction_warnings": [],
  "dosage": {
    "source": "standard_reference",
    "amount": "500mg",
    "frequency": "every 6 hours",
    "disclaimer": "General reference information, not personalized medical advice."
  }
}
```

`interaction_warnings`, when non-empty, looks like:

```json
"interaction_warnings": [
  {
    "warning_type": "duplicate_ingredient",
    "detail": "Also contains Paracetamol, same as 'Dolo 650' already on file."
  }
]
```

`dosage` when unavailable (combination drug, or no reference found — see the open question in `Design/Backend/db-schema.md`):

```json
"dosage": {
  "source": "unavailable",
  "amount": null,
  "frequency": null,
  "disclaimer": null
}
```

### `status: "resolved"`, expired — no dosage, ever

```json
{
  "status": "resolved",
  "scan_type": "medicine",
  "user_medicine_id": "6f9c...",
  "medicine": {
    "brand_name": "Calpol",
    "chemical_components": ["Paracetamol"]
  },
  "expiry": {
    "expired": true,
    "expiring_soon": false,
    "printed_expiry_date": "2024-01-15"
  },
  "interaction_warnings": [],
  "dosage": null,
  "message": "This medicine has expired. Please get a new one."
}
```

Note `dosage` is `null` here, not an "unavailable" object — expired medicines never get a dosage suggestion, full stop (REQ-14). The Android app should treat these as distinct cases: "unavailable" invites a caretaker follow-up later; expired is a hard stop.

### `status: "pending"` — couldn't classify or identify the medicine

```json
{
  "status": "pending",
  "scan_artifact_id": "a12b...",
  "message": "We couldn't confidently read this. It's been saved for review."
}
```

There is currently **no resolution path** for a pending scan (Phase 2's caretaker Web UI doesn't exist yet) — the Android app should show this as a calm, non-alarming message, not an error state. `scan_artifact_id` is returned for forward compatibility (so it's already in the response shape once a resolution flow exists), but is **write-only from the app's perspective right now** — there is no endpoint to poll it for a status change, and the app should not attempt to.

### Request errors — `4xx`/`5xx`, standard error shape

```json
{
  "error": {
    "code": "missing_image",
    "message": "No image was provided in the request."
  }
}
```

Reserved for genuine request problems — never for a scan that simply couldn't be resolved.

| HTTP status | `error.code` | When |
|---|---|---|
| 400 | `missing_image` | `image` field absent from the request |
| 400 | `invalid_account_id` | `account_id` isn't a well-formed UUID |
| 400 | `unsupported_image_type` | `image` content type isn't `image/jpeg` or `image/png` |
| 413 | `image_too_large` | `image` exceeds 10 MB |
| 500 | `internal_error` | Anything unexpected (DB down, unhandled exception, etc.) |

Full implementation detail (exact Pydantic models, FastAPI route code) lives in the [API unit](../Backend/units/API/README.md) — this document is the wire contract, that one is how it's built.

## Pydantic schema reference

These are the exact request/response models the `API` unit uses — see that unit's design for the route handler that builds them. The request isn't a single Pydantic model (FastAPI reads multipart form fields individually via `Form(...)`/`File(...)`, not a JSON body), but the fields correspond to:

```python
class ScanRequestFields(BaseModel):
    """Not the actual route signature (see the API unit) — documents the
    shape of the incoming form fields for reference."""
    account_id: UUID
    client_scan_type: Literal["medicine", "unknown"]
    detected_brand_name: Optional[str] = None
    detected_expiry_date: Optional[date] = None
    ocr_raw_text: Optional[str] = None
    # `image` is a separate File(...) upload, not part of this model.
```

```python
class MedicineIdentity(BaseModel):
    brand_name: str
    chemical_components: list[str]

class ExpiryInfo(BaseModel):
    expired: bool
    expiring_soon: bool
    printed_expiry_date: Optional[date]

class InteractionWarningOut(BaseModel):
    warning_type: str
    detail: str

class DosageOut(BaseModel):
    source: Literal["standard_reference", "unavailable"]
    amount: Optional[str]
    frequency: Optional[str]
    disclaimer: Optional[str]

class ResolvedScanResponse(BaseModel):
    status: Literal["resolved"] = "resolved"
    scan_type: Literal["medicine"] = "medicine"
    user_medicine_id: UUID
    medicine: MedicineIdentity
    expiry: ExpiryInfo
    interaction_warnings: list[InteractionWarningOut]
    dosage: Optional[DosageOut]        # None only when expiry.expired is True
    message: Optional[str] = None      # populated only in the expired case

class PendingScanResponse(BaseModel):
    status: Literal["pending"] = "pending"
    scan_artifact_id: UUID
    message: str

class ErrorDetail(BaseModel):
    code: str
    message: str

class ErrorResponse(BaseModel):
    error: ErrorDetail
```
