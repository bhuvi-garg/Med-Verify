# REQ-06 — Dosage Intake Reminder

Status: Draft — pending review

## Description

When a prescription has been scanned and its dosage schedule extracted (REQ-05), the system reminds the user when it is time to take each medicine, based on the frequency/timing written on the prescription.

Per [REQ-00](REQ-00-behavior-model.md): this requirement only applies when a prescription has actually been scanned. There is no fallback to a "default" intake schedule for reminders — without an extracted frequency/timing, the app has no basis to schedule anything and simply does not create a reminder.

## Delivery channel (decided)

In-app only for v1 (no push/SMS).

## Adherence tracking (decided)

The app tracks adherence — whether each scheduled dose was acknowledged as taken. Your answer noted the reminder repeats "every day till a bill is uploaded" — documented here as: an unacknowledged/overdue reminder keeps repeating daily rather than silently expiring. **Flagging for confirmation**: tying the repeat condition to a bill upload sounds more like the refill flow (REQ-08, "keep nagging to buy a refill until a new bill is scanned") than to acknowledging a single dose. Please confirm whether daily intake reminders should just repeat until acknowledged (independent of any bill), or whether they're genuinely meant to stop only once a new bill is uploaded.

## Schedule lifetime (decided)

Both: the reminder schedule ends automatically when the prescription's course duration is up, and the user can also cancel it manually at any time before that.

## Acceptance criteria

- Given an extracted dosage schedule (e.g. "twice daily", "every 8 hours"), the system schedules and delivers in-app reminders at the appropriate times.
- Reminders identify which medicine to take and the dosage amount.
- Each reminder's acknowledgment (taken / not taken) is recorded to support adherence tracking.
- An unacknowledged reminder repeats daily until resolved (see confirmation note above on what "resolved" means).
- The reminder schedule ends automatically when the prescription's course duration elapses, or earlier if the user cancels it manually.
- If no prescription has been scanned for a medicine, no intake reminder is created or expected — this is not an error state.
