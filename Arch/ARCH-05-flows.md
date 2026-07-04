# ARCH-05 — Key Flows

Status: Draft — pending review

## Scan flow

Covers [REQ-01](../Requirements/REQ-01-input-classification.md) through the relevant downstream requirement depending on classification.

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
        SVC->>DB: read / write USER_MEDICINE,<br/>PRESCRIPTION_ITEM, BILL_ITEM
        SVC-->>API: structured result<br/>(+ dosage / warnings)
        API-->>App: response
        App->>App: render large text +<br/>play translated TTS audio
    end
```

## Reminder & escalation flow

Covers [REQ-06](../Requirements/REQ-06-dosage-reminder.md), [REQ-08](../Requirements/REQ-08-refill-reminder.md), and [REQ-13](../Requirements/REQ-13-missed-dose-escalation.md).

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
    box rgb(191,106,2) External
        participant SMS as ☎️ SMS Gateway
    end

    loop on cadence
        SCHED->>DB: find due reminders
        DB-->>SCHED: due REMINDER rows
        SCHED->>App: push in-app reminder
        App-->>SCHED: acknowledgment (later)
        SCHED->>DB: write ADHERENCE_LOG
        Note over SCHED,DB: REQ-06 course ends →<br/>reminder auto-cancelled

        alt missed beyond per-medicine threshold
            SCHED->>DB: read ESCALATION_CONTACT
            SCHED->>SMS: send escalation SMS
            SMS-->>SCHED: delivery status
            SCHED->>DB: write ESCALATION_EVENT
        end
    end
```

## Notes

- The scheduler drives reminders proactively (push), rather than the app polling for them — needed since REQ-06/REQ-13 must function even if the app isn't actively open.
- Refill reminders (REQ-08) follow the same shape as the reminder loop above, but are seeded by `BILL_ITEM` quantity + whichever dosage source is available (REQ-00's fallback order), rather than a prescription's frequency field directly.
