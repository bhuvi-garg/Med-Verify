# REQ-09 — AI Chat Follow-up

Status: Approved — **deferred to Phase 3, not in initial scope**

## Description

An AI-powered chat box that lets the user ask follow-up questions about a scanned medicine or prescription (e.g. side effects, interactions, clarifying questions about dosage).

## Why Phase 3, separate from Phase 1/Phase 2 (decided)

Phase 1 and Phase 2 are both deterministic and scripted — every requirement in this set through REQ-08 and REQ-10–REQ-17 behaves predictably given its inputs, with no probabilistic component. That's deliberate: Phase 1 and Phase 2 are built and demoed together (e.g. for a competition), and their behavior needs to be fully explainable and repeatable. An AI chat box is the one feature in this set that's inherently probabilistic (an LLM's response isn't guaranteed reproducible), so it's pushed out to its own **Phase 3**, kept explicitly out of the Phase 1/2 demo scope, rather than being folded into "Phase 2" alongside otherwise-deterministic features like the caretaker Web UI.

## Model choice (decided direction)

Local-language support (REQ-03) is core to this app, and it's an Indian-market product — so the underlying AI model should be evaluated from Indian-language-first models rather than defaulting to a generic Western LLM. Prioritize models such as **Sarvam AI** (built specifically for Indian languages) as the leading candidate, over general-purpose models that may not handle Indian regional languages as well.

## Notes

- Explicitly called out as a Phase 3 feature, not part of Phase 1 or Phase 2.
- No acceptance criteria defined yet — to be scoped when this feature is scheduled.
- When scoping, evaluate Indian AI providers (Sarvam AI first) against the actual local-language list settled in REQ-03, rather than assuming a single global model will cover all of them well.
