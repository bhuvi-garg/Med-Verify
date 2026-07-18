# Unit: Classification

Status: Draft — pending review

## Responsibility

Decides medicine vs. not-confidently-medicine (REQ-01), **on-device**. Only this binary decision is implemented in this slice — prescription/bill classification is out of scope. Runs immediately after [OCR](../OCR/README.md), before anything is uploaded.

## Location

`app/src/main/java/com/medverify/scan/ScanClassifier.kt`

## Depends on

Nothing else in this system (leaf unit).

## Constants

```kotlin
const val CONFIDENCE_THRESHOLD: Float = 0.6f
```

## Public interface

```kotlin
enum class ScanClassification { MEDICINE, UNKNOWN }

fun classifyScan(ocrResult: OcrResult): ScanClassification {
    // Returns MEDICINE if ocrResult.confidence >= CONFIDENCE_THRESHOLD
    // AND ocrResult.detectedBrandName is a non-blank string.
    // Otherwise returns UNKNOWN.
}
```

## Algorithm

```
1. if ocrResult.confidence < CONFIDENCE_THRESHOLD: return UNKNOWN
2. if ocrResult.detectedBrandName.isNullOrBlank(): return UNKNOWN
3. return MEDICINE
```

## Test cases

| # | `confidence` | `detectedBrandName` | Expected |
|---|---|---|---|
| 1 | 0.9 | `"Calpol"` | `MEDICINE` |
| 2 | 0.6 (exactly threshold) | `"Calpol"` | `MEDICINE` — threshold is inclusive |
| 3 | 0.59 | `"Calpol"` | `UNKNOWN` |
| 4 | 0.9 | `null` | `UNKNOWN` |
| 5 | 0.9 | `""` | `UNKNOWN` |
| 6 | 1.0 | `"  "` (whitespace only) | `UNKNOWN` — `isBlank()` covers this |

## What happens after classification (not this unit's job)

Regardless of the result, the Android app always uploads the image to the backend (see [Design/Android/units/README.md](../README.md)), along with `ocrResult`'s fields — a `MEDICINE` classification with a resolvable brand leads to a normal resolved response; `UNKNOWN`, or a `MEDICINE` classification whose brand the backend still can't resolve, both end up as a pending review item. Neither case is ever surfaced to the elderly user as a choice to make (REQ-11/REQ-17).

## Open questions

- `CONFIDENCE_THRESHOLD = 0.6` is a placeholder — needs tuning once a real OCR library is chosen (see [OCR](../OCR/README.md)'s open questions) and its confidence scores can be observed against real scans.
