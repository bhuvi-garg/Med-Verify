# Med-Verify

Med-Verify scans medicines, prescriptions, and pharmacy bills to help patients — especially elderly users — understand what they're taking, how much of it, and when.

It reads what's printed on a medicine's packaging back to the user in large text and audio, in their own local language; extracts dosage schedules from prescriptions and reminds the user when to take each dose; and tracks pharmacy bills to remind them to refill before running out. A caretaker can supervise all of it from a separate web dashboard.

## What it does

Every scan is exactly one of three independent input types — there's no combined input, and none of them need to happen in any particular order:

| Scan | What happens |
|---|---|
| **Medicine** | Identifies the medicine by its chemical/active ingredient (so different brands of the same drug — e.g. Paracetamol, Calpol, Dolo — are recognized as equivalent), reads the label back in large text and translated audio, and suggests an elderly-appropriate dosage. |
| **Prescription** | Extracts the dosage, frequency, and course duration the doctor wrote, and sets up intake reminders that end automatically when the course does. |
| **Pharmacy Bill** | Extracts the medicine and quantity purchased, and — combined with whatever dosage information is known — sets up a refill reminder before the supply runs out. |

```mermaid
%%{init: {"flowchart": {"curve": "basis"}, "themeVariables": {"fontSize": "15px"}} }%%
flowchart LR
    subgraph SCANS["📷 Scan one of three"]
        direction TB
        M["💊 Medicine"]
        RX["📋 Prescription"]
        BILL["🧾 Pharmacy Bill"]
    end

    CORE(["🗄️ Med-Verify<br/>persistent per-medicine knowledge"])

    subgraph OUT["What comes out"]
        direction TB
        O1["Large text + audio<br/>label readback"]
        O2["Elderly-appropriate dosage"]
        O3["Intake & refill reminders"]
        O4["Interaction / expiry warnings"]
        O5["Caretaker escalation"]
    end

    M --> CORE
    RX --> CORE
    BILL --> CORE
    CORE --> O1
    CORE --> O2
    CORE --> O3
    CORE --> O4
    CORE --> O5

    classDef scan fill:#1f6feb,stroke:#123a75,color:#ffffff,stroke-width:1.5px;
    classDef core fill:#8250df,stroke:#5a32a3,color:#ffffff,stroke-width:1.5px;
    classDef out fill:#2ea043,stroke:#1a6b30,color:#ffffff,stroke-width:1.5px;
    classDef groupBox fill:transparent,stroke:#57606a,stroke-width:1px,stroke-dasharray: 4 3,color:#57606a,font-weight:bold;

    class M,RX,BILL scan;
    class CORE core;
    class O1,O2,O3,O4,O5 out;
    class SCANS,OUT groupBox;
```

The app remembers everything it learns per medicine, across separate scans, and fills in gaps as more information becomes available — e.g. a bill scanned before its matching prescription still gets a best-effort refill reminder, which is automatically corrected once the prescription is scanned later, even if it was purchased under a different brand name.

**Safety checks run automatically on every scan**: duplicate/interacting medicines are flagged, an expired medicine never gets a dosage suggestion (only a warning to replace it), and a caretaker is escalated to — by SMS and a real-time dashboard alert — if scheduled doses are repeatedly missed.

## Who it's for

- **The elderly patient** is the only day-to-day user, and is kept out of every piece of complexity that isn't the scan itself: no login, no settings, no manual data entry, and no denser "advanced" screen to accidentally wander into.
- **A caretaker** (family member) performs one-time setup — creating the account, scanning the patient's existing prescriptions/bills, registering an emergency contact — and can be linked to multiple elderly users from a single account (e.g. one caretaker looking after both parents).
- Anything the system can't confidently read, or any dosage/reminder that needs a human check, is handled entirely on the caretaker's side — never by asking the elderly user to type something in.

## Phases

```mermaid
%%{init: {"flowchart": {"curve": "basis"}, "themeVariables": {"fontSize": "15px"}} }%%
flowchart LR
    subgraph DEMO["🎯 Competition Demo Scope"]
        direction LR
        P1["📱 Phase 1<br/>Android App"]
        P2["🌐 Phase 2<br/>Caretaker Web UI"]
    end
    P3["🤖 Phase 3<br/>AI Chat<br/><i>(out of demo scope)</i>"]

    P1 ==> P2
    P2 -. "the one probabilistic addition" .-> P3

    classDef phase1 fill:#1f6feb,stroke:#123a75,color:#ffffff,stroke-width:1.5px;
    classDef phase2 fill:#8250df,stroke:#5a32a3,color:#ffffff,stroke-width:1.5px;
    classDef phase3 fill:#bf6a02,stroke:#7a4400,color:#ffffff,stroke-width:1.5px;
    classDef demoBox fill:transparent,stroke:#d4a72c,stroke-width:2.5px,stroke-dasharray: 6 3,color:#d4a72c,font-weight:bold;

    class P1 phase1;
    class P2 phase2;
    class P3 phase3;
    class DEMO demoBox;
```

| Phase | Contains | Notes |
|---|---|---|
| **Phase 1** | The Android app: all three scan types, dosage suggestions, intake/refill reminders, safety checks, SMS escalation. | Fully deterministic and scripted. |
| **Phase 2** | The caretaker web dashboard: multi-patient linking, real-time escalation alerts, and reviewing/overriding anything the app couldn't resolve or got wrong. | Also fully deterministic — built and demoed together with Phase 1. |
| **Phase 3** | An AI chat box for follow-up questions, prioritizing Indian-language-first models (e.g. Sarvam AI) over generic Western LLMs. | The only probabilistic piece in the system — deliberately kept out of the Phase 1/2 demo scope for that reason. |

## Repository structure

Requirements and architecture are fully specified; implementation hasn't started.

- **[Requirements/](Requirements/README.md)** — the full, approved requirement set (REQ-00 through REQ-17) — what the app must do, and why.
- **[Arch/](Arch/README.md)** — the full, approved architecture — client/backend/data-layer design, entity model, and every key flow as a diagram.
- **[Design/](Design/README.md)** — UI/UX design, split into `Android/`, `WebUI/`, `Backend/`, and `Interop/` (the frontend↔backend JSON contract) — not yet started.
- **[Implementation/](Implementation/README.md)** — source code, mirroring the same `Android/`, `WebUI/`, `Backend/`, `Interop/` split (each of the first three also has its own `config/`) — not yet started.
- **[Test/](Test/README.md)** — test plans and test code (not yet started).
- **[Knowledge/](Knowledge/README.md)** — beginner-friendly guides to WSL, VS Code, and Git/GitHub, written for student contributors new to these tools.
- `.claude/` — Claude Code project-local config.
