# REQ-10 — Caretaker Web Dashboard

Status: Approved — **deferred to Phase 2, not in initial scope**

## Description

Since the app is aimed at elderly users, a caretaker (family member/guardian) should be able to track the elderly person's medicine, prescription, and reminder information via a separate web application, rather than only through the elderly user's own device.

This implies:

- A login/account system for the elderly user (not required for Phase 1, where the app can be assumed single-user/local; initial account setup itself is handled in-app by the caretaker per REQ-15).
- A web-based view for the caretaker to see the same information the app tracks (scanned medicines, prescriptions, reminders, adherence, escalation events from REQ-13).
- The ongoing, caretaker-facing management complexity — e.g. adding/editing medicines after initial setup — belongs **here**, not in the elderly-facing app. Per REQ-15, the app itself deliberately does not support a caretaker routinely returning to add medicines one by one; that responsibility lives in this dashboard so the elderly user's app can stay minimal and low-risk of getting them stuck on the wrong screen.

## Caretaker has their own account, not the elderly user's (decided)

The caretaker logs into this dashboard with **their own account** — not by sharing the elderly user's credentials. One caretaker account can be linked to **multiple** elderly users (see [REQ-16](REQ-16-caretaker-multi-patient-linking.md)), so a caretaker looking after several relatives manages all of them from one login. On login, the dashboard's first screen is a list of every elderly user linked to that caretaker; the caretaker picks one to view/manage its detail.

## Notes

- Explicitly deferred to Phase 2 — not part of the initial release, along with REQ-16.
- No further acceptance criteria defined yet — to be scoped when this feature is scheduled. Will need to account for authentication and data sync between the mobile app and the web server, on top of the account-linking behavior specified in REQ-16.
