# Implementation — Interop

Shared interop artifacts that both frontends (Android, WebUI) and the Backend actually build against — e.g. JSON Schema files, or generated types/clients — implementing what [Design/Interop](../../Design/Interop/README.md) specifies.

The goal is a single source of truth: a change here should be the *only* place a wire-format change needs to happen, with each component's code generated from or validated against it, rather than each side hand-maintaining its own copy.

Not started yet.
