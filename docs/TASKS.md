# Task Register

**Current active phase:** Phase 3 Completed — Ready for Phase 4 (`M4.1`: Pluggable Grounded AI Assistant)  
**Phase 0 completion:** 100% (8 of 8 tasks completed)  
**Phase 1 completion:** 100% (4 of 4 tasks completed)  
**Phase 2 completion:** 100% (4 of 4 tasks completed)  
**Phase 3 completion:** 100% (4 of 4 tasks completed)  
**Overall project completion:** **70%** (Phases 0, 1, 2, and 3 complete; 30% remaining across Phases 4 and 8)  

---

## Phase 0 tasks (`M0.1` – `M0.5`: 100% Completed)

| ID | Milestone | Task | Deliverable | Owner | Estimate | Actual | Deps | Status | Evidence & Test Result | Risk & Next Action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **T0.1** | M0.1 | Identify recurring technical workflows | Ranked workflow table (1–6) with frequency, time lost, and memory targets | Architect | 1–2h | 0.4h | None | **Completed** | Verified in [docs/DISCOVERY.md](DISCOVERY.md) §1 | Low risk; seeded in `StarterKnowledgeCatalog` |
| **T0.2** | M0.2 | Audit development environment | Verified tool inventory for Antigravity VM + separated user-reported Windows 11 context | Engineer | 1h | 0.5h | None | **Completed** | Read-only shell audit (`java 17.0.21`, `git 2.39.5`, `ssh 9.2p1`, `19GB` free disk) recorded in [docs/DISCOVERY.md](DISCOVERY.md) §2 | Resolved in `T1.1` |
| **T0.3** | M0.3 | Audit Android, Termux, and Debian `proot` boundaries | 4-environment boundary map and sandbox analysis | Engineer / Security | 1–2h | 0.3h | T0.2 | **Completed** | Documented UID/SELinux isolation and `pkg` vs `apt` rules in [docs/DISCOVERY.md](DISCOVERY.md) §3.1–3.2 | Enforced in `ExecutionEnvironmentType` |
| **T0.4** | M0.3 | Map SSH connection and network requirements | Windows -> Termux `sshd` (`8022`) flow and sleep/IP/Doze failure modes | Engineer | 1h | 0.2h | T0.3 | **Completed** | Documented in [docs/DISCOVERY.md](DISCOVERY.md) §3.3–3.4 | Seeded in starter workflows |
| **T0.5** | M0.4 | Compare app architecture options | Evidence-based comparison table (Kotlin/Compose vs cross-platform vs companion daemons) | Architect | 1–2h | 0.3h | T0.1–T0.4 | **Completed** | Documented in [docs/DISCOVERY.md](DISCOVERY.md) §4 and `ADR 0004`–`0008` in [docs/DECISIONS.md](DECISIONS.md) | Native Kotlin + Compose built |
| **T0.6** | M0.4 | Define data model, UX IA, and security boundaries | Room schema, 4-tab UX IA, Execution Context Card spec, Secret Guard rules | Architect / UX / Security | 1–2h | 0.3h | T0.5 | **Completed** | Updated [docs/ARCHITECTURE.md](ARCHITECTURE.md), [docs/SECURITY.md](SECURITY.md), and `ADR 0009` | Implemented in Phase 1 & 2 |
| **T0.7** | M0.4 | Recommend MVP and Phase 1 scope | Complete Phase 0 Final Report & updated roadmap/milestones/tasks | Delivery Mgr | 1h | 0.2h | T0.1–T0.6 | **Completed** | Updated [docs/DISCOVERY.md](DISCOVERY.md) §6, [docs/ROADMAP.md](ROADMAP.md), [docs/MILESTONES.md](MILESTONES.md) | Completed |
| **T0.8** | M0.5 | Owner review and approval | Recorded owner decision unlocking Phase 1 | **Owner** | ~5 min | 0.1h | T0.7 | **Completed** | Owner approved Phase 0 and authorized Phase 1 (`2026-11-12`) | Unlocked Phase 1 |

---

## Phase 1 tasks (`M1.1`: Android Foundation & UX Shell — 100% Completed)

| ID | Milestone | Task | Deliverable | Estimate | Actual | Deps | Status | Evidence & Test Result |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **T1.1** | M1.1 | Configure Android build toolchain & scaffold Kotlin/Compose project | Gradle Wrapper (`./gradlew`), `app/build.gradle.kts`, `.gitignore`, `.github/workflows/android-ci.yml`, and user-space Android SDK 35 in VM | 1.0h | 0.3h | T0.8 | **Completed** | Gradle 8.11.1 + AGP 8.7.2 + Kotlin 2.0.21 + Android SDK 35 verified |
| **T1.2** | M1.1 | Implement core domain enums, models, and `SecretGuardValidator` | `NexusModels.kt`, `SecretGuardValidator.kt`, `StarterKnowledgeCatalog.kt`, and `SecretGuardAndModelTest.kt` | 1.0h | 0.3h | T1.1 | **Completed** | `./gradlew testDebugUnitTest` passed 5/5 JUnit tests verifying secret blocking & Termux/Debian separation |
| **T1.3** | M1.1 | Build Material 3 theme, 4-tab navigation shell, and 7-part `ExecutionContextCard` UI | `NexusTheme.kt`, `ExecutionContextCard.kt`, `NexusAppShell.kt` (`Recall`, `Library`, `Troubleshoot`, `Systems`), `MainActivity.kt` | 1.0h | 0.3h | T1.2 | **Completed** | Compiled with zero warnings or errors |
| **T1.4** | M1.1 | Run full Phase 1 verification & package debug APK | Passing unit tests, assembled `app-debug.apk`, and updated milestone docs | 0.5h | 0.1h | T1.3 | **Completed** | `./gradlew testDebugUnitTest assembleDebug` exited `0` and built `app/build/outputs/apk/debug/app-debug.apk` |

---

## Phase 2 tasks (`M2.1`: Technical Memory Schema & Starter Pack — 100% Completed)

| ID | Milestone | Task | Deliverable | Estimate | Actual | Deps | Status | Evidence & Test Result |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **T2.1** | M2.1 | Define Room SQLite database and DAOs | `NexusDatabase.kt`, `NexusEntities.kt` (devices, environments, recipes, cases, workflows), `NexusDaos.kt` | 1.0h | 0.3h | T1.4 | **Completed** | KSP compiled Room DAOs with reactive Kotlin Coroutine Flows |
| **T2.2** | M2.1 | Pre-seed starter knowledge into Room vault | Auto-seeding callback in `NexusDatabase` loading Pixel 7 Pro, Windows 11, Termux, Debian proot | 1.0h | 0.3h | T2.1 | **Completed** | Pre-seeded on first run; verified by `StarterKnowledgeCatalog` catalog tests |
| **T2.3** | M2.1 | Build Backup & Restore Manager with Secret Guard | `NexusBackupManager.kt` supporting full JSON and Markdown exports and SecretGuard-enforced imports | 1.0h | 0.2h | T2.2 | **Completed** | Unit tests verify JSON round-trip and blocking of leaked SSH keys / tokens |
| **T2.4** | M2.1 | Wire Repository, Add-Recipe UI, and run Phase 2 verification | `NexusRepository.kt`, updated `NexusAppShell.kt`, `RoomAndBackupTest.kt`, assembled `app-debug.apk` | 1.0h | 0.2h | T2.3 | **Completed** | 9/9 unit tests passing (`100%`), `app-debug.apk` (56MB) assembled |

---

## Phase 3 tasks (`M3.1`: Search, Verification & Guided Workflows — 100% Completed)

| ID | Milestone | Task | Deliverable | Estimate | Actual | Deps | Status | Evidence & Test Result |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **T3.1** | M3.1 | Wire instant Room search & environment pill filtering | Real-time multi-field query matching across titles, purposes, syntax, and environments | 1.0h | 0.2h | T2.4 | **Completed** | Search bar instantly filters recipes and cases across environments |
| **T3.2** | M3.1 | Implement one-tap Recipe Verification flow | Toggle verification state (`VERIFIED`, `UNVERIFIED_DRAFT`) with immediate badge update | 1.0h | 0.2h | T3.1 | **Completed** | One-tap button updates Room SQLite and persists across restarts |
| **T3.3** | M3.1 | Implement interactive multi-step Workflow Runner | Step-by-step checklist with persistent active step state and Troubleshooter copy shortcuts | 1.5h | 0.3h | T3.2 | **Completed** | Step check state saved to SQLite; copy shortcuts for safe check & fix |
| **T3.4** | M3.1 | Run Phase 3 acceptance tests & package debug APK | `SearchAndWorkflowTest.kt`, assemble debug APK | 0.5h | 0.1h | T3.3 | **Completed** | 14/14 unit tests passing (`100%`), `app-debug.apk` (56MB) assembled |

## Phase 4 tasks (`M4.1`: Grounded AI Assistant & Secret Redaction — 100% Completed)

| ID | Milestone | Task | Deliverable | Estimate | Actual | Deps | Status | Evidence & Test Result |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **T4.1** | M4.1 | Define `AiAssistantProvider` interface and citations model | `AiAssistantProvider.kt` with `AssistantCitation` and `AssistantDiagnosticResponse` | 1.0h | 0.3h | T3.4 | **Completed** | Clean contract for local/remote assistants with citations and risk warnings |
| **T4.2** | M4.1 | Implement `OfflineRetrievalProvider` | Deterministic local vault search ranking cases and verified recipes | 1.0h | 0.3h | T4.1 | **Completed** | 100% offline retrieval with zero network calls and grounded local citations |
| **T4.3** | M4.1 | Implement `ContextRedactor` and Context Preview Dialog | SecretGuard-backed sanitization, prompt injection defense, and destructive command detection | 1.0h | 0.2h | T4.2 | **Completed** | Verified: blocks passwords, API tokens, flags `rm -rf /`, `mkfs`, `chmod 777` |
| **T4.4** | M4.1 | Wire Assistant into UI & run Phase 4 unit tests | Interactive card in `TroubleshootScreen`, `AiAssistantTest.kt`, assemble debug APK | 1.0h | 0.2h | T4.3 | **Completed** | 20/20 unit tests passing (`100%`), `app-debug.apk` (56MB) assembled |

## Phase 5 tasks (`M5.1`: Live Read-Only Environment Inspector — 100% Completed)

| ID | Milestone | Task | Deliverable | Estimate | Actual | Deps | Status | Evidence & Test Result |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **T5.1** | M5.1 | Build localhost SSH probe & Wi-Fi IP auto-detection | `EnvironmentInspector.kt` (`probeLocalPort`, `detectWifiIp`, `getBatteryInfo`) | 1.0h | 0.3h | T8.4 | **Completed** | Non-blocking socket connect to `127.0.0.1:8022`; network interface IP extraction |
| **T5.2** | M5.1 | Define Termux Allowlisted Read-Only Command Bridge | `TermuxBridge.kt` with `TermuxCommandSpec` and strict allowlist | 1.0h | 0.2h | T5.1 | **Completed** | Strictly allows read-only checks (`uptime`, `free -h`); rejects dangerous commands |
| **T5.3** | M5.1 | Wire Live Health Inspector into Systems Screen | Interactive card in `SystemsScreen` with live port badge, Copy IP button, and re-probe | 1.0h | 0.3h | T5.2 | **Completed** | Live status card renders port status, local Wi-Fi IP, and battery charging state |
| **T5.4** | M5.1 | Write Phase 5 unit tests & assemble APK v0.10.0 | `EnvironmentInspectionTest.kt`, assemble debug APK | 1.0h | 0.2h | T5.3 | **Completed** | 26/26 unit tests passing (`100%`), `nexus-v0.10.0.apk` (56MB) assembled |

## Phase 8 tasks (`M8.1`: Personal Release & Owner Playbook — 100% Completed)

| ID | Milestone | Task | Deliverable | Estimate | Actual | Deps | Status | Evidence & Test Result |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **T8.1** | M8.1 | Package Personal Release APK `v0.10.0` | `app/build.gradle.kts` updated to `versionCode = 10`, `versionName = "0.10.0"` | 0.5h | 0.1h | T4.4 | **Completed** | Gradle build outputs verified release-ready `app-debug.apk` (56MB) |
| **T8.2** | M8.1 | End-to-end Vault Backup & Restore verification | Verification of JSON & Markdown export/import routines with Secret Guard scanner | 0.5h | 0.2h | T8.1 | **Completed** | Full round-trip tested; blocks leaked SSH keys/tokens; restores recipes & cases |
| **T8.3** | M8.1 | Write 5-minute Owner Playbook | `docs/OWNER_PLAYBOOK.md` detailing Pixel 7 Pro APK installation, daily 4-tab usage, and SSH cheat-sheet | 1.0h | 0.3h | T8.2 | **Completed** | Plain-English non-technical guide published in `docs/OWNER_PLAYBOOK.md` |
| **T8.4** | M8.1 | Final Acceptance & Documentation sync | Update `README.md`, `ROADMAP.md`, `MILESTONES.md`, and push to GitHub | 0.5h | 0.1h | T8.3 | **Completed** | 26/26 unit tests pass, pushed to `main` |
| **T8.5** | M8.2 | Official NEXUS Ribbon Logo & 2026 Light Color System | `NexusLogo.kt` composable, `NexusColors` in `NexusTheme.kt` with light-first slate/white/emerald/sky palette | 1.0h | 0.3h | T8.4 | **Completed** | Clean folded ribbon logo, high-contrast light colors matching Style 1 (`image.png`) |
| **T8.6** | M8.2 | Redesign Splash Screen & Genuine Loading Experience | `SplashScreen.kt`, wire real Room & environment probe in `MainActivity.kt` with crossfade | 1.0h | 0.3h | T8.5 | **Completed** | Genuine initialization stages only, zero artificial delays, seamless transition |
| **T8.7** | M8.2 | Refine Recall Home Dashboard & Quick Actions Grid | Refined `RecallScreen` in `NexusAppShell.kt` with greeting, pill search, live status banner, 4 quick actions, environment chips | 1.5h | 0.4h | T8.6 | **Completed** | Preserved all existing functionality; 26/26 unit tests passing; built `nexus-v0.11.0.apk` |

### Post-MVP Optional Work (Phases 6, 7 & 9)
- **Phase 6 (`M6.1`):** Add typed allowlisted operations, per-action approval modal, execution timeouts, output limits, and audit verification.
- **Phase 7 (`M7.1`):** Evaluate Windows 11 companion only if manual SSH handoff proves insufficient.
- **Phase 9 (`M9.1`):** Commercial feasibility research only after personal utility is demonstrated.

---

## Task update format

For each task record: status, owner, estimate, actual effort, dependencies, evidence, test result, risk, and next action.

