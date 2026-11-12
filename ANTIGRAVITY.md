# Antigravity Operating Guide

## Mission

Build NEXUS incrementally as a personal technical memory and guided operations assistant. The first goal is a reliable product definition and validated architecture, not an immediate APK.

## Start here

1. Read README.md and AGENTS.md.
2. Read all documents in docs/.
3. Inspect repository status and branch.
4. Identify the current milestone in docs/MILESTONES.md.
5. Work only on the active milestone.

## Phase 0 assignment

Perform discovery and produce evidence. Do not begin implementation.

### Discovery work

- Inspect the available development environment without changing it.
- Determine whether Android Studio, Android SDK, JDK, Gradle, ADB, Git, and required command line tools are installed.
- Record versions and exact availability. Do not infer installation from PATH alone.
- Map the current Windows, Android, Termux, and Debian proot boundaries.
- Explain how Windows SSH connects to the Termux sshd service and what network conditions are required.
- Explain why an Android app cannot automatically execute arbitrary Termux or Debian commands because of Android application sandboxing.
- Compare native Android app options, including Kotlin with Jetpack Compose, and justify the recommendation.
- Evaluate local storage options such as Room over SQLite.
- Evaluate secure secret storage and device pairing.
- Compare direct device access, an authenticated companion service, and user initiated copy and paste workflows.
- Identify which capabilities are feasible for a first personal release and which require a separate companion component.
- Document all assumptions and unresolved questions.

### Deliverables

Update:
- docs/DISCOVERY.md
- docs/ARCHITECTURE.md
- docs/DECISIONS.md
- docs/ROADMAP.md
- docs/MILESTONES.md
- docs/TASKS.md

Add a concise Phase 0 report to docs/DISCOVERY.md with evidence, recommendation, confidence, and unresolved risks.

### Approval gate

Present findings and request approval before Phase 1. Do not start coding the app before approval.

## Development discipline

- Prefer simple, maintainable architecture.
- Avoid unnecessary cloud services and recurring costs for a personal MVP.
- Do not add accounts, subscriptions, multi tenant infrastructure, or analytics unless validated as necessary.
- Keep AI providers replaceable.
- Treat model output as untrusted input.
- Do not pass model generated text directly to a shell.
- Use allowlisted operation definitions, parameter validation, explicit approval, timeouts, output limits, redaction, and audit logs for any future execution feature.
- Every operation must identify its target device and environment.

## Completion reporting

At each milestone end, report completion percentage, changed files, tests, verified behavior, risks, and remaining work. Keep user acceptance checks to about five minutes when practical. If critical checks pass, proceed to the next approved task rather than repeating the same checks.
