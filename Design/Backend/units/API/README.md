# Unit: API

Status: Draft — pending review

## Responsibility

The **only** unit exposed over HTTP. Validates requests, calls [Scan](../Scan/README.md), and translates results to the JSON responses specified in [Design/Interop/scan-endpoint.md](../../../Interop/scan-endpoint.md). Nothing outside this unit knows FastAPI, Pydantic, or HTTP status codes exist.

## Location

`app/api/routes/scan.py` (route handler)
`app/schemas/scan.py` (Pydantic models — full definitions in [Design/Interop/scan-endpoint.md](../../../Interop/scan-endpoint.md#pydantic-schema-reference))

## Depends on

[Scan](../Scan/README.md). Nothing else — this unit does not talk to the database directly, does not call `MedicineResolution`/`Dosage`/etc. directly, and does not import `Utility` directly (any storage/normalization need belongs inside `Scan` or below).

## Public interface

This unit's "interface" is the HTTP route itself:

```python
@router.post("/scan", response_model=ResolvedScanResponse | PendingScanResponse)
async def scan_endpoint(
    account_id: str = Form(...),
    client_scan_type: Literal["medicine", "unknown"] = Form(...),
    detected_brand_name: Optional[str] = Form(None),
    detected_expiry_date: Optional[date] = Form(None),
    ocr_raw_text: Optional[str] = Form(None),
    image: UploadFile = File(...),
    db: Session = Depends(get_db),
) -> ResolvedScanResponse | PendingScanResponse:
    ...
```

Note there's no `ocr_adapter` dependency anymore — the backend never performs OCR itself (see [Design/Android/units/OCR](../../../Android/units/OCR/README.md)), so `Scan` no longer needs one injected.

## Algorithm

```
scan_endpoint(account_id, client_scan_type, detected_brand_name, detected_expiry_date,
              ocr_raw_text, image, db):
    1. Validate account_id is a well-formed UUID.
       if not: raise HTTPException(400, ErrorResponse(error=ErrorDetail(
           code="invalid_account_id", message="account_id must be a valid UUID")))

    2. Validate image.content_type in {"image/jpeg", "image/png"}.
       if not: raise HTTPException(400, ErrorResponse(error=ErrorDetail(
           code="unsupported_image_type",
           message="Only image/jpeg and image/png are accepted.")))

    3. image_bytes = await image.read()
       if len(image_bytes) == 0:
           raise HTTPException(400, ErrorResponse(error=ErrorDetail(
               code="missing_image", message="No image was provided in the request.")))
       if len(image_bytes) > 10 * 1024 * 1024:
           raise HTTPException(413, ErrorResponse(error=ErrorDetail(
               code="image_too_large", message="Image exceeds the 10MB limit.")))

    4. scan_input = ScanInput(
           account_id=UUID(account_id), image_bytes=image_bytes,
           client_scan_type=client_scan_type, detected_brand_name=detected_brand_name,
           detected_expiry_date=detected_expiry_date, ocr_raw_text=ocr_raw_text)
       result = ScanService(db).process_scan(scan_input)

    5. if isinstance(result, PendingScanResult):
           return PendingScanResponse(scan_artifact_id=result.scan_artifact_id,
                                       message=result.message)

    6. if isinstance(result, ResolvedScanResult):
           return ResolvedScanResponse(
               user_medicine_id=result.user_medicine_id,
               medicine=MedicineIdentity(
                   brand_name=result.medicine_brand_name,
                   chemical_components=result.chemical_components),
               expiry=ExpiryInfo(
                   expired=result.expired, expiring_soon=result.expiring_soon,
                   printed_expiry_date=result.printed_expiry_date),
               interaction_warnings=[
                   InteractionWarningOut(warning_type=w.warning_type, detail=w.detail)
                   for w in result.interaction_warnings],
               dosage=(DosageOut(**asdict(result.dosage)) if result.dosage else None),
               message=("This medicine has expired. Please get a new one."
                        if result.expired else None),
           )

    # Any unhandled exception anywhere above this point is caught by a global
    # FastAPI exception handler, logged, and turned into:
    #   500, ErrorResponse(error=ErrorDetail(code="internal_error", message="..."))
    # — not handled inline in this function.
```

## Test cases

Using FastAPI's `TestClient`, with `Scan`'s dependency swapped for a stub/mock so this unit's tests only exercise *this* unit's logic (validation + response translation), not the full pipeline:

| # | Request | Expected |
|---|---|---|
| 1 | no `image` field | `400`, `error.code == "missing_image"` |
| 2 | `account_id="not-a-uuid"` | `400`, `error.code == "invalid_account_id"` |
| 3 | `image` with `content_type="application/pdf"` | `400`, `error.code == "unsupported_image_type"` |
| 4 | `image` of 11 MB | `413`, `error.code == "image_too_large"` |
| 5 | `client_scan_type="banana"` (not a valid literal) | `422` — handled automatically by FastAPI/Pydantic's `Literal` validation, before this unit's own code runs |
| 6 | valid request, `client_scan_type="medicine"`, `detected_brand_name="Calpol"`, `Scan` mock returns a `ResolvedScanResult` (not expired) | `200`, response body matches `ResolvedScanResponse` shape, `message` is `null` |
| 7 | valid request, `Scan` mock returns a `ResolvedScanResult` with `expired=True` | `200`, `dosage` is `null`, `message` is the expired-medicine text |
| 8 | valid request, `client_scan_type="unknown"`, `detected_brand_name` omitted, `Scan` mock returns a `PendingScanResult` | `200`, response body matches `PendingScanResponse` shape |
| 9 | valid request, `Scan` mock raises an unexpected exception | `500`, `error.code == "internal_error"`, and the exception is logged (assert via caplog or similar) |

## Notes

- This unit is intentionally "dumb" — no business logic, no database access, no domain decisions. If a test for this unit needs to know anything about chemical components or dosage fallback order, that logic has leaked in from where it belongs (`Scan` and below) and should be moved back out.
