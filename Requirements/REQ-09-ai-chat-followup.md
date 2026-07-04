# REQ-09 — AI Chat Follow-up

Status: Draft — **deferred to a later version, not in initial scope**

## Description

An AI-powered chat box that lets the user ask follow-up questions about a scanned medicine or prescription (e.g. side effects, interactions, clarifying questions about dosage).

## Model choice (decided direction)

Local-language support (REQ-03) is core to this app, and it's an Indian-market product — so the underlying AI model should be evaluated from Indian-language-first models rather than defaulting to a generic Western LLM. Prioritize models such as **Sarvam AI** (built specifically for Indian languages) as the leading candidate, over general-purpose models that may not handle Indian regional languages as well.

## Notes

- Explicitly called out as a later-version feature, not part of the initial release.
- No acceptance criteria defined yet — to be scoped when this feature is scheduled.
- When scoping, evaluate Indian AI providers (Sarvam AI first) against the actual local-language list settled in REQ-03, rather than assuming a single global model will cover all of them well.
