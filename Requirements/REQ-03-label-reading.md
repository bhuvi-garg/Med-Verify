# REQ-03 — Label Reading (Text + Audio, Multi-language)

Status: Draft — pending review

## Description

When the scanned input is a medicine (REQ-01), the system must read out what is printed on the medicine's packaging/label and present it back to the user in two forms:

- **Large text** on screen (for readability, e.g. for visually impaired or elderly users)
- **Audio narration**, available in different local/regional languages

## Acceptance criteria

- The text printed on the medicine packaging is extracted (assumed to be in English as printed) and displayed in an enlarged, readable font.
- The extracted English text is translated into the user's selected local language before narration — narration is not limited to reading the label as printed.
- The same content can be played back as audio, via a text-to-speech engine, in the user's selected local language.
- The user can choose the language for the audio narration from a set of supported local languages.

## Open questions

- Which local languages must be supported at launch? Still to be defined — planned for a later pass, needed before Design can pick a translation/TTS provider.
- Text-to-speech engine/voice source: expected to be a standard TTS engine, but the specific choice is deferred to Design.
