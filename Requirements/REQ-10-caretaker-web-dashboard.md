# REQ-10 — Caretaker Web Dashboard

Status: Draft — **deferred to Phase 2, not in initial scope**

## Description

Since the app is aimed at elderly users, a caretaker (family member/guardian) should be able to track the elderly person's medicine, prescription, and reminder information via a separate web application, rather than only through the elderly user's own device.

This implies:

- A login/account system for the elderly user (not required for Phase 1, where the app can be assumed single-user/local; initial account setup itself is handled in-app by the caretaker per REQ-15).
- A web-based view for the caretaker to see the same information the app tracks (scanned medicines, prescriptions, reminders, adherence, escalation events from REQ-13).
- The ongoing, caretaker-facing management complexity — e.g. adding/editing medicines after initial setup — belongs **here**, not in the elderly-facing app. Per REQ-15, the app itself deliberately does not support a caretaker routinely returning to add medicines one by one; that responsibility lives in this dashboard so the elderly user's app can stay minimal and low-risk of getting them stuck on the wrong screen.

## Notes

- Explicitly deferred to Phase 2 — not part of the initial release.
- No acceptance criteria defined yet — to be scoped when this feature is scheduled. Will need to account for authentication, data sync between the mobile app and the web server, and caretaker/elderly-user account linking.
