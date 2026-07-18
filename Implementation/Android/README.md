# Implementation — Android

The Android app source code (Phase 1). See [Design/Android](../../Design/Android/README.md) for the screen/navigation design this should follow, and [Design/Interop](../../Design/Interop/README.md) for the API shapes it talks to (or mocks, before the Backend exists).

## Current state

Kotlin + Jetpack Compose + Material3 project, buildable with the Gradle wrapper in this directory. Implemented so far:

- **First-launch login/setup form** (`app/src/main/java/com/medverify/android/login/`) — a one-time
  screen collecting Full Name, Phone Number, Age, Gender, Height (optional), Weight, Emergency
  Contact Number, and Pharmacy Contact Number (optional). Shown once via a Jetpack DataStore
  completion flag; skipped on every subsequent launch. Not an authentication screen — there's no
  password or account login yet, just the caretaker-entered patient profile referenced elsewhere
  in the design as onboarding.
- **Placeholder home screen** (`app/src/main/java/com/medverify/android/home/`) — shown after
  setup is complete, confirms the saved profile by displaying it. Scanning/dosage features are not
  built yet.

These profile fields don't yet correspond to any entity in `Design/Backend/db-schema.md` (whose
`account` table only has `id`/`username`/`created_at`); the data is stored locally via DataStore
for now, not synced to a backend.

## Building

```bash
cd Implementation/Android
./gradlew assembleDebug
```

Requires a `local.properties` file (not committed) pointing `sdk.dir` at your Android SDK
location. To actually run the app, open this directory as a project in Android Studio and launch
it on an emulator or device — no AVD/emulator is set up in this repo's dev environment.
