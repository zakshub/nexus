# Task Register

**Current active phase:** Phase 0 (Awaiting Owner Approval on `T0.8` to unlock Phase 1)  
**Phase 0 completion:** 88% (7 of 8 tasks completed; `T0.8` is Owner Approval)  
**Overall project completion:** 10% (Phase 0 discovery completed; Phases 1–8 pending approval)  

---

## Phase 0 tasks (`M0.1` – `M0.5`)

| ID | Milestone | Task | Deliverable | Owner | Estimate | Actual | Deps | Status | Evidence & Test Result | Risk & Next Action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **T0.1** | M0.1 | Identify recurring technical workflows | Ranked workflow table (1–6) with frequency, time lost, and memory targets | Architect | 1–2h | 0.4h | None | **Completed** | Verified in [docs/DISCOVERY.md](DISCOVERY.md) §1 | Low risk; ready for Phase 2 starter pack seeding |
| **T0.2** | M0.2 | Audit development environment | Verified tool inventory for Antigravity VM + separated user-reported Windows 11 context | Engineer | 1h | 0.5h | None | **Completed** | Read-only shell audit (`java 17.0.21`, `git 2.39.5`, `ssh 9.2p1`, `19GB` free disk; no Android SDK/Gradle pre-installed) recorded in [docs/DISCOVERY.md](DISCOVERY.md) §2 | VM requires user-space Android SDK in `T1.1` |
| **T0.3** | M0.3 | Audit Android, Termux, and Debian `proot` boundaries | 4-environment boundary map and sandbox analysis | Engineer / Security | 1–2h | 0.3h | T0.2 | **Completed** | Documented UID/SELinux isolation and `pkg` vs `apt` rules in [docs/DISCOVERY.md](DISCOVERY.md) §3.1–3.2 | Mitigated by mandatory Environment Badge on every recipe |
| **T0.4** | M0.3 | Map SSH connection and network requirements | Windows -> Termux `sshd` (`8022`) flow and sleep/IP/Doze failure modes | Engineer | 1h | 0.2h | T0.3 | **Completed** | Documented in [docs/DISCOVERY.md](DISCOVERY.md) §3.3–3.4 | Mitigated by pre-seeding SSH & wake-lock workflows |
| **T0.5** | M0.4 | Compare app architecture options | Evidence-based comparison table (Kotlin/Compose vs cross-platform vs companion daemons) | Architect | 1–2h | 0.3h | T0.1–T0.4 | **Completed** | Documented in [docs/DISCOVERY.md](DISCOVERY.md) §4 and `ADR 0004`–`0008` in [docs/DECISIONS.md](DECISIONS.md) | None; Native Kotlin + Compose selected |
| **T0.6** | M0.4 | Define data model, UX IA, and security boundaries | Room schema, 4-tab UX IA, Execution Context Card spec, Secret Guard rules | Architect / UX / Security | 1–2h | 0.3h | T0.5 | **Completed** | Updated [docs/ARCHITECTURE.md](ARCHITECTURE.md), [docs/SECURITY.md](SECURITY.md), and `ADR 0009` | Enforce Secret Guard unit tests in Phase 1 |
| **T0.7** | M0.4 | Recommend MVP and Phase 1 scope | Complete Phase 0 Final Report & updated roadmap/milestones/tasks | Delivery Mgr | 1h | 0.2h | T0.1–T0.6 | **Completed** | Updated [docs/DISCOVERY.md](DISCOVERY.md) §6, [docs/ROADMAP.md](ROADMAP.md), [docs/MILESTONES.md](MILESTONES.md) | Ready for owner review |
| **T0.8** | M0.5 | Owner review and approval | Recorded owner decision unlocking Phase 1 | **Owner** | ~5 min | Pending | T0.7 | **Awaiting Owner Approval** | Phase 0 Final Report presented | **Stop Gate:** Do not start `T1.1` until approved |

---

## Phase 1 tasks (`M1.1`: Android Foundation & UX Shell — Blocked until `T0.8` Approved)

| ID | Milestone | Task | Deliverable | Estimate | Deps | Acceptance Criteria (<= 5 min check) |
| --- | --- | --- | --- | --- | --- | --- |
| **T1.1** | M1.1 | Configure Android build toolchain & scaffold Kotlin/Compose project | Gradle Wrapper (`./gradlew`), `app/build.gradle.kts`, `.gitignore`, `.github/workflows/android-ci.yml`, and user-space Android SDK in VM | 1.0h | T0.8 | `./gradlew help` and project sync succeed with JDK 17 |
| **T1.2** | M1.1 | Implement core domain enums, models, and `SecretGuardValidator` | Strongly typed models (`DevicePlatform`, `EnvironmentType`, `ImpactLevel`, `VerificationStatus`) + `SecretGuardValidator` with JUnit tests | 1.0h | T1.1 | `./gradlew testDebugUnitTest` passes 100% of Secret Guard and model boundary tests |
| **T1.3** | M1.1 | Build Material 3 theme, 4-tab navigation shell, and 7-part `ExecutionContextCard` UI | Compose screens (`Recall`, `Library`, `Troubleshoot`, `Systems`) and reusable `ExecutionContextCard` showing Device, Environment, Prompt check, Purpose, Expected Output, Verification, and Safety badges | 1.0h | T1.2 | All 4 tabs navigate cleanly; Context Card renders distinct badges for Termux Host vs Debian `proot` vs Windows 11 |
| **T1.4** | M1.1 | Run full Phase 1 verification & package debug APK | Passing unit tests, assembled `app-debug.apk`, and updated milestone docs | 0.5h | T1.3 | `./gradlew testDebugUnitTest assembleDebug` exits `0` and produces `app-debug.apk` |

---

## Future task groups (Phases 2 to 9)

### Phase 2 (`M2.1`: Technical Memory & Starter Pack)
- `T2.1`: Define Room database (`NexusDatabase`), entities, FTS4 virtual table (`RecipeFtsEntity`), and DAOs.
- `T2.2`: Pre-seed curated starter knowledge base for Windows 11, Pixel 7 Pro, Termux (`sshd` on port `8022`, `termux-wake-lock`, `pkg`), and Debian `proot` (`proot-distro login debian`, `apt`).
- `T2.3`: Implement SAF JSON + Markdown knowledge base export and import with `SecretGuardValidator` enforcement.
- `T2.4`: Run DAO, FTS4 search, and export/import unit tests.

### Phase 3 (`M3.1`: Search, Recipes & Resumable Guided Workflows)
- `T3.1`: Wire instant FTS4 search and top Environment Pill Filter (`All`, `Termux Host`, `Debian proot`, `Windows 11`).
- `T3.2`: Implement Recipe create/edit/verify flows and one-tap Copy with Prompt Verification checklist + Audit Log recording.
- `T3.3`: Implement stateful step-by-step Workflow Runner with persistent progress (`DataStore` + `Room`) that resumes after app close.
- `T3.4`: Implement Troubleshooter (`Symptom -> Plain-English Cause -> Read-Only Check -> Verified Fix`) screen and tests.

### Phase 4 (`M4.1`: Contextual Grounded Assistant)
- `T4.1`: Define `AiAssistantProvider` interface and default `OfflineRetrievalProvider` (100% offline).
- `T4.2`: Implement `ContextRedactor` and mandatory **Context Preview Dialog** showing exact redacted text before any remote call.
- `T4.3`: Implement opt-in `RemoteGeminiProvider` (using Android Keystore for API key) and `✨ AI Suggested — Unverified` response cards with local recipe citations.
- `T4.4`: Test hallucination warnings, high-risk command flagging, and prompt-injection defense.

### Phases 5 to 9 (Post-MVP & Optional)
- **Phase 5 (`M5.1`):** Select and prototype one permissioned read-only inspection workflow (Termux `RUN_COMMAND` Intent or localhost SSH `127.0.0.1:8022`).
- **Phase 6 (`M6.1`):** Add typed allowlisted operations, per-action approval modal, execution timeouts, output limits, and audit verification.
- **Phase 7 (`M7.1`):** Evaluate Windows 11 companion only if manual SSH handoff proves insufficient.
- **Phase 8 (`M8.1`):** Package personal release APK, backup/recovery guide, and 5-minute owner acceptance test.
- **Phase 9 (`M9.1`):** Commercial feasibility research only after personal value is demonstrated.

---

## Task update format

For each task record: status, owner, estimate, actual effort, dependencies, evidence, test result, risk, and next action.

