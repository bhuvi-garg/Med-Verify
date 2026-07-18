# Unit: OCR

Status: Draft — pending review

## Responsibility

Extracts text/brand/expiry/confidence from a captured image, **on-device**. Runs on the Android app only — the backend never re-runs OCR (see [ARCH-00](../../../../Arch/ARCH-00-overview.md#why-not-put-logic-on-device-and-the-one-exception)).

## Location

`app/src/main/java/com/medverify/ocr/OcrEngine.kt`

## Depends on

Nothing else in this system (leaf unit). Depends on an on-device OCR library (see Open Questions).

## Public interface

```kotlin
data class OcrResult(
    val rawText: String,
    val detectedBrandName: String?,
    val detectedExpiryDate: LocalDate?,
    val confidence: Float  // 0.0 - 1.0
)

interface OcrEngine {
    fun extract(imageBytes: ByteArray): OcrResult
}
```

## Implementation (this slice)

For development and unit testing, a fixture-backed fake implementation, mirroring the backend's `StubOCRAdapter` pattern:

```kotlin
class FakeOcrEngine(private val cannedResult: OcrResult) : OcrEngine {
    override fun extract(imageBytes: ByteArray): OcrResult = cannedResult
}

val DEFAULT_DEV_OCR_RESULT = OcrResult(
    rawText = "CALPOL 500mg\nEXP 03/2027",
    detectedBrandName = "Calpol",
    detectedExpiryDate = LocalDate.of(2027, 3, 1),
    confidence = 0.9f
)
```

## Test cases

Same purpose as the backend's OCR unit — no logic of its own to test in the fake, but it lets every unit downstream of it (`Classification`, and eventually the upload step) be tested against a range of fixtures without a real OCR library:

| # | Fixture | Used to test |
|---|---|---|
| 1 | high confidence, brand present, expiry far future | happy path through `Classification` and the upload step |
| 2 | low confidence | `Classification`'s "unknown" branch |
| 3 | high confidence, unrecognizable/garbled brand text | brand not resolvable once it reaches the backend's `MedicineResolution` unit |
| 4 | `detectedExpiryDate = null` | uploads with a null expiry field — expiry logic lives entirely on the backend (`Expiry` unit), not here |

## Open questions

- Real on-device OCR library choice — not decided yet. The obvious default for Android is Google's **ML Kit Text Recognition** (on-device, free, no network call needed, well-supported) but this hasn't been formally evaluated or confirmed.
- ML Kit (or any general OCR library) recognizes *text*, not structured fields like "brand name" or "expiry date" directly — turning raw recognized text into `detectedBrandName`/`detectedExpiryDate` needs its own parsing logic (e.g. regex for date patterns, matching against a bundled list of known brand names) that isn't designed yet. This is a real gap, not a minor detail — worth its own design pass before implementation.
