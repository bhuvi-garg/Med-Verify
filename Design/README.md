# Design

How Med-Verify will be built — UI/UX design and detailed design decisions that turn the [Requirements/](../Requirements/README.md) and [Arch/](../Arch/README.md) into a concrete plan each component can be implemented from.

Organized to mirror the architecture's client/backend split:

- **[Android/](Android/README.md)** — UI/UX design for the elderly-facing Android app (Phase 1), especially the always-simplified interface (REQ-11) and the no-manual-entry rule (REQ-17).
- **[WebUI/](WebUI/README.md)** — UI/UX design for the caretaker Web UI (Phase 2).
- **[Backend/](Backend/README.md)** — backend design decisions more granular than Arch's system-level view (e.g. specific endpoint validation and error-handling behavior).
- **[Interop/](Interop/README.md)** — the frontend↔backend interop contract: the actual JSON shapes exchanged over the API, building on the endpoint list in [Arch/ARCH-01](../Arch/ARCH-01-components.md) and the entities in [Arch/ARCH-03](../Arch/ARCH-03-data-model.md).

Not started yet beyond this scaffolding.
