# Implementation

Source code for Med-Verify, once [Design/](../Design/README.md) is worked out for a given piece.

Organized to mirror the architecture's client/backend split:

- **[Android/](Android/README.md)** — the Android app source (Phase 1).
- **[WebUI/](WebUI/README.md)** — the caretaker Web UI source (Phase 2).
- **[Backend/](Backend/README.md)** — the Python/FastAPI backend source.
- **[Interop/](Interop/README.md)** — shared interop artifacts (e.g. JSON Schema files) that both frontends and the Backend actually build against, implementing what [Design/Interop](../Design/Interop/README.md) specifies.

Each of `Android/`, `WebUI/`, and `Backend/` has its own `config/` subfolder, for that component's actual configuration (environment variables, build settings) once it exists.

Not started yet beyond this scaffolding.
