# ARCH-05 — Key Flows

Status: Approved

Five flows cover the system end to end: how an account gets set up, how a scan is processed, how the two reminder types work, and how a missed dose/refill escalates to a caretaker.

## 1. Onboarding & account setup flow

Covers [REQ-15](../Requirements/REQ-15-assisted-onboarding.md) — the caretaker performs this on the elderly user's device.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"actorBkg": "#1f6feb", "actorBorder": "#123a75", "actorTextColor": "#ffffff", "actorLineColor": "#57606a", "signalColor": "#57606a", "signalTextColor": "#1f2328", "labelBoxBkgColor": "#8250df", "labelBoxBorderColor": "#5a32a3", "labelTextColor": "#ffffff", "noteBkgColor": "#fff8c5", "noteBorderColor": "#bf8700"}} }%%
sequenceDiagram
    autonumber
    box rgb(31,111,235) Client
        participant App as 📱 Android App<br/>(caretaker-operated)
    end
    box rgb(46,160,67) Backend
        participant API as API Layer
        participant SVC as Service Layer
    end
    participant DB as 🗄️ PostgreSQL

    App->>API: create account (username / password)
    API->>SVC: register_account()
    SVC->>DB: write ACCOUNT
    DB-->>SVC: account_id
    SVC-->>API: session + refresh token
    API-->>App: signed-in session<br/>(stored in Android Keystore)
    Note right of App: elderly user never sees<br/>a login screen again (ARCH-02)

    loop for each initial prescription / bill
        App->>API: POST /scan (image)
        Note over API,DB: same steps as the Scan Flow below
        API->>SVC: classify + extract + persist
        SVC->>DB: write USER_MEDICINE,<br/>PRESCRIPTION_ITEM / BILL_ITEM
    end

    App->>API: add escalation contact(s)
    API->>SVC: save_contact()
    SVC->>DB: write ESCALATION_CONTACT

    opt caretaker provides their own identifier
        App->>API: link caretaker (email/phone)
        API->>SVC: request_caretaker_link()
        SVC->>DB: write CARETAKER_LINK<br/>(status: pending or active)
        Note right of App: optional, non-blocking (REQ-16, Phase 2)<br/>can also be done/changed later
    end
```

## 2. Scan flow

Covers [REQ-01](../Requirements/REQ-01-input-classification.md) through the relevant downstream requirement depending on classification, plus the automatic safety checks that run alongside it.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"actorBkg": "#1f6feb", "actorBorder": "#123a75", "actorTextColor": "#ffffff", "actorLineColor": "#57606a", "signalColor": "#57606a", "signalTextColor": "#1f2328", "labelBoxBkgColor": "#8250df", "labelBoxBorderColor": "#5a32a3", "labelTextColor": "#ffffff", "noteBkgColor": "#fff8c5", "noteBorderColor": "#bf8700"}} }%%
sequenceDiagram
    autonumber
    box rgb(31,111,235) Client
        participant App as 📱 Android App
    end
    box rgb(46,160,67) Backend
        participant API as API Layer
        participant SVC as Service Layer
    end
    participant DB as 🗄️ PostgreSQL
    box rgb(191,106,2) External
        participant EXT as ☁️ OCR/Translation/TTS
    end

    App->>API: POST /scan (image)
    API->>SVC: classify(image)
    SVC->>EXT: extract text/data
    EXT-->>SVC: extracted text
    SVC->>SVC: classify as medicine / prescription / bill

    alt low confidence
        SVC-->>API: ambiguous
        API-->>App: prompt manual type selection
        Note right of App: REQ-01 fallback —<br/>user picks the type
    else confident
        SVC->>SVC: route to domain logic<br/>(REQ-02 / REQ-05 / REQ-07)
        SVC->>DB: resolve USER_MEDICINE<br/>by chemical identity
        SVC->>SVC: check expiry (REQ-14)
        alt expired
            SVC-->>API: expired warning only —<br/>no dosage suggestion
        else not expired
            SVC->>SVC: check duplicate ingredient /<br/>interaction (REQ-12)
            SVC->>DB: write USER_MEDICINE,<br/>PRESCRIPTION_ITEM, BILL_ITEM
            SVC-->>API: structured result<br/>(+ dosage / warnings)
        end
        API-->>App: response
        App->>App: render large text +<br/>play translated TTS audio
    end
```

## 3. Intake reminder flow

Covers [REQ-06](../Requirements/REQ-06-dosage-reminder.md) — only exists once a prescription has been scanned.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"actorBkg": "#2ea043", "actorBorder": "#1a6b30", "actorTextColor": "#ffffff", "actorLineColor": "#57606a", "signalColor": "#57606a", "signalTextColor": "#1f2328", "labelBoxBkgColor": "#8250df", "labelBoxBorderColor": "#5a32a3", "labelTextColor": "#ffffff", "noteBkgColor": "#fff8c5", "noteBorderColor": "#bf8700"}} }%%
sequenceDiagram
    autonumber
    box rgb(46,160,67) Backend
        participant SCHED as ⏱️ Scheduler
    end
    participant DB as 🗄️ PostgreSQL
    box rgb(31,111,235) Client
        participant App as 📱 Android App
    end

    loop on cadence
        SCHED->>DB: find due REMINDER<br/>(type = intake)
        DB-->>SCHED: due rows
        SCHED->>App: push in-app reminder
        App-->>SCHED: acknowledgment<br/>(taken / not taken)
        SCHED->>DB: write ADHERENCE_LOG

        alt prescription course duration elapsed
            SCHED->>DB: cancel reminder (REQ-06)
        else missed beyond per-medicine threshold
            SCHED->>SCHED: trigger Escalation Flow (§5)
        end
    end
```

## 4. Refill reminder flow

Covers [REQ-08](../Requirements/REQ-08-refill-reminder.md) — seeded by a bill's purchased quantity, using whichever dosage source is available per [REQ-00](../Requirements/REQ-00-behavior-model.md)'s fallback order.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"actorBkg": "#2ea043", "actorBorder": "#1a6b30", "actorTextColor": "#ffffff", "actorLineColor": "#57606a", "signalColor": "#57606a", "signalTextColor": "#1f2328", "labelBoxBkgColor": "#8250df", "labelBoxBorderColor": "#5a32a3", "labelTextColor": "#ffffff", "noteBkgColor": "#fff8c5", "noteBorderColor": "#bf8700"}} }%%
sequenceDiagram
    autonumber
    box rgb(46,160,67) Backend
        participant SCHED as ⏱️ Scheduler
    end
    participant DB as 🗄️ PostgreSQL
    box rgb(31,111,235) Client
        participant App as 📱 Android App
    end

    loop on cadence
        SCHED->>DB: read BILL_ITEM quantity +<br/>best available dosage source
        DB-->>SCHED: prescription dosage,<br/>else standard dosage, else none
        SCHED->>SCHED: compute expected run-out date

        alt no dosage source available
            Note over SCHED: no reminder created —<br/>not an error (REQ-00)
        else within 5 days of run-out
            SCHED->>DB: create/update REMINDER<br/>(type = refill)
            SCHED->>App: push in-app refill reminder
            Note right of App: labeled prescription-based<br/>or estimated (REQ-08)
        end

        alt overdue beyond threshold, no new bill scanned
            SCHED->>SCHED: trigger Escalation Flow (§5)
        end
    end
```

## 5. Escalation flow

Covers [REQ-13](../Requirements/REQ-13-missed-dose-escalation.md) — triggered by either the intake flow (§3) or the refill flow (§4). Notifies the saved contact by SMS **and** alerts the caretaker in the Web UI (Phase 2), not one or the other.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"actorBkg": "#2ea043", "actorBorder": "#1a6b30", "actorTextColor": "#ffffff", "actorLineColor": "#57606a", "signalColor": "#57606a", "signalTextColor": "#1f2328", "labelBoxBkgColor": "#8250df", "labelBoxBorderColor": "#5a32a3", "labelTextColor": "#ffffff", "noteBkgColor": "#fff8c5", "noteBorderColor": "#bf8700"}} }%%
sequenceDiagram
    autonumber
    box rgb(46,160,67) Backend
        participant SCHED as ⏱️ Scheduler
        participant API as API Layer
    end
    participant DB as 🗄️ PostgreSQL
    box rgb(191,106,2) External
        participant SMS as ☎️ SMS Gateway
    end
    box rgb(31,111,235) Client
        participant WebUI as 🌐 Web UI<br/>(Caretaker, Phase 2)
    end

    Note over SCHED,DB: entered from the intake flow (§3)<br/>or the refill flow (§4)

    SCHED->>DB: read ESCALATION_CONTACT
    SCHED->>DB: write ESCALATION_EVENT (status: new)

    par notify by SMS
        SCHED->>SMS: send escalation SMS
        SMS-->>SCHED: delivery status
    and notify Web UI in real time
        SCHED->>API: publish escalation alert
        API-->>WebUI: push alert (WebSocket/SSE)
        Note right of WebUI: shown immediately if the<br/>caretaker is online, persisted<br/>either way for later viewing
    end

    WebUI->>API: (later) fetch escalation history
    API->>DB: read ESCALATION_EVENT
    DB-->>API: event rows
    API-->>WebUI: render list (read / unread)
```

## Notes

- The scheduler drives both reminder types proactively (push), rather than the app polling for them — needed since REQ-06/REQ-08/REQ-13 must function even if no client is actively open.
- Intake (§3) and refill (§4) reminders are kept as separate flows because their lifecycles differ: intake ends at course completion, refill has no course-completion concept yet (open question, see [ARCH-06](ARCH-06-scan-combination-behavior.md)).
- Escalation (§5) is shared logic — both reminder types feed into the same flow rather than each implementing their own notification path, so SMS + Web UI delivery only needs to be built once.
- The Web UI alert path (§5) is Phase 2 — until REQ-10 ships, escalation is SMS-only, but `ESCALATION_EVENT` is written from day one so history is complete once the Web UI arrives.
