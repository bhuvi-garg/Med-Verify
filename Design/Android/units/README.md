# Android Unit Breakdown

Status: Draft — pending review

## Scope

Only two units live on the Android side of the medicine-scan slice, per [Arch/ARCH-00](../../../Arch/ARCH-00-overview.md#why-not-put-logic-on-device-and-the-one-exception)'s test: a feature belongs on-device only if it's consumed by exactly one frontend and never touches shared/persisted state. OCR and initial scan classification (REQ-01) are the only pieces of scan-processing logic that qualify — everything else (chemical-identity resolution, dosage, interaction/expiry checks) needs the backend's shared reference data and per-account history, and is designed in [Design/Backend/units/](../../Backend/units/README.md).

## Units

| Unit | Responsibility |
|---|---|
| [OCR](OCR/README.md) | Extracts text/brand/expiry/confidence from a captured image, on-device. |
| [Classification](Classification/README.md) | Decides medicine vs. not-confidently-medicine from the OCR unit's output, on-device. |

## How this connects to the backend

Regardless of what these two units conclude, the Android app always uploads the image to the backend, along with whatever fields it managed to extract — see [Design/Interop/scan-endpoint.md](../../Interop/scan-endpoint.md) for the exact request shape, and [Design/Backend/units/Scan/README.md](../../Backend/units/Scan/README.md) for what the backend does with it.

```mermaid
%%{init: {"flowchart": {"curve": "basis"}, "themeVariables": {"fontSize": "15px"}} }%%
flowchart LR
    CAM["📷 Camera capture"] --> OCR["OCR<br/><i>on-device</i>"]
    OCR --> CLASS["Classification<br/><i>on-device</i>"]
    CLASS --> UPLOAD["⬆️ Always upload:<br/>image + extracted fields"]
    UPLOAD -.-> BACKEND["🐍 Backend<br/>(Design/Backend/units/)"]

    classDef android fill:#1f6feb,stroke:#123a75,color:#ffffff,stroke-width:1.5px;
    classDef backend fill:#2ea043,stroke:#1a6b30,color:#ffffff,stroke-width:1.5px;

    class CAM,OCR,CLASS,UPLOAD android;
    class BACKEND backend;
```

## Language/tooling note

Not formally pinned yet the way [Design/Backend/tech-stack.md](../../Backend/tech-stack.md) pins the backend stack — these unit designs assume Kotlin (the standard choice for Android development) and JUnit for unit tests, as reasonable defaults rather than a deliberated decision. A dedicated `Design/Android/tech-stack.md` should be written before implementation starts, covering the OCR library choice in particular (see [OCR](OCR/README.md)'s open questions).
