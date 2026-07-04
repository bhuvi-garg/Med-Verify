# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Med-Verify: scans medicine and tells proper dosage.

## Current state

This repository is in the pre-implementation phase. No source code, build system, or dependencies exist yet. Do not assume any language, framework, or tooling until it's actually introduced — check for config files (package.json, requirements.txt, etc.) before recommending commands.

## Repository structure

The repo is organized around project phases rather than code modules:

- `Requirements/` — what the system must do
- `Design/` — how it will be built (architecture decisions, UI/UX design)
- `Arch/` — architecture artifacts/diagrams
- `Implementation/` — actual source code, once started
- `Test/` — test plans and test code
- `.claude/` — Claude Code project-local config/memory

When starting implementation work, check `Requirements/` and `Design/` first for any specs before writing code, since they are the intended source of truth for what to build.

## Commit message convention

- Title: `mv: <Commit Title>`, under 40 characters total.
- Body: one `-` bulleted line per point, each under 80 characters.
- Prefer separate bullet lines over prose paragraphs.
