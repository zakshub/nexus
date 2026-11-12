# Roadmap

**Current status:** Phase 0 (`100%`), Phase 1 (`100%`), Phase 2 (`100%`), Phase 3 (`100%`), Phase 4 (`100%`), and Phase 8 (`100%`) completed (**`100%` Personal MVP Complete!**). Core personal technical assistant is fully built, tested, packaged, and documented.

| Phase | Name | Goal | Est. Effort | Status | Completion |
| --- | --- | --- | --- | --- | --- |
| **0** | **Discovery and validation** | Verify environment, use cases, boundaries, UX, security, and architecture | 2.5 hours | **Completed (`2026-11-12`)** | **100%** |
| **1** | **Android foundation & UX shell** | Create Kotlin/Compose app shell, 4-tab navigation, Execution Context Card UI, unit tests, and CI build pipeline | 3–4 hours | **Completed (`2026-11-12`)** | **100%** |
| **2** | **Technical memory & starter pack** | Room SQLite database, Secret Guard, JSON/MD export & restore, and pre-seeded Pixel 7 Pro / Termux / Debian / Windows 11 memory | 4–5 hours | **Completed (`2026-11-12`)** | **100%** |
| **3** | **Search & guided workflows** | Instant environment-filtered search, recipe verification flows, and resumable step-by-step workflows | 4–5 hours | **Completed (`2026-11-12`)** | **100%** |
| **4** | **Contextual AI assistant** | Pluggable `AiAssistantProvider` with 100% offline retrieval, secret sanitization, high-risk detection, and citations | 3–4 hours | **Completed (`2026-11-12`)** | **100%** |
| **5** | **Read-only inspection** | Permissioned Termux Intent or localhost SSH read-only environment health checks | 4–6 hours | Postponed (Post-MVP) | 0% |
| **6** | **Approved operations** | Typed allowlisted operations with per-action approval, timeouts, redaction, and audit trail | 5–7 hours | Postponed (Post-MVP) | 0% |
| **7** | **Windows companion** | Optional Windows 11 bridge if manual SSH handoff proves insufficient | 6–8 hours | Optional / Deferred | 0% |
| **8** | **Personal release** | Signed/installable personal APK (`v0.8.0-final`), full backup/restore verification, and 5-min owner playbook | 2–3 hours | **Completed (`2026-11-12`)** | **100%** |
| **9** | **Commercial feasibility** | Evaluate broader market demand only after personal utility is proven | TBD | Optional research | 0% |

---

## Phase 0: Discovery and validation (Completed — Pending Sign-Off)

**Goal:** Decide what should be built and prove the integration boundaries before writing application code.

Deliverables completed (`2026-11-12`):
- [x] Verified Antigravity VM environment inventory and documented user-reported physical device inventory ([docs/DISCOVERY.md](DISCOVERY.md))
- [x] Prioritized user workflows ranked by frequency, time lost, and failure impact ([docs/DISCOVERY.md](DISCOVERY.md))
- [x] Android sandbox, Termux host, Debian `proot`, and Windows 11 SSH boundary analysis ([docs/DISCOVERY.md](DISCOVERY.md))
- [x] Security threat model, Secret Guard specification, and AI redaction rules ([docs/SECURITY.md](SECURITY.md))
- [x] Architecture recommendation and ADRs `0001`–`0009` ([docs/ARCHITECTURE.md](ARCHITECTURE.md), [docs/DECISIONS.md](DECISIONS.md))
- [x] Non-technical UX information architecture (`Recall`, `Library`, `Troubleshoot`, `Systems`) and 7-part Execution Context Card design ([docs/DISCOVERY.md](DISCOVERY.md), [docs/ARCHITECTURE.md](ARCHITECTURE.md))
- [x] Revised effort estimates and exact Phase 1 implementation plan ([docs/MILESTONES.md](MILESTONES.md), [docs/TASKS.md](TASKS.md))
- [ ] Owner approval to proceed to Phase 1 (`M0.5` / `T0.8`)

---

## Exact Phase 1 Implementation Plan (Upon Owner Approval)

**Goal:** Stand up a clean, reproducible Kotlin + Jetpack Compose Android project (`com.zakshub.nexus`) with the 4-tab navigation structure, reusable Execution Context Card UI components, domain models for Devices/Environments/Badges, unit tests, and automated APK generation.

1. **Task `T1.1` — Build Toolchain & Project Scaffolding (`~1 hour`):**
   - Configure Android command-line SDK tools (`platforms;android-35`, `build-tools;35.0.0`) in `/home/antigravity/android-sdk` (user space) and add a Gradle Wrapper (`./gradlew`, Gradle 8.x, AGP 8.x, Kotlin 2.x, JDK 17).
   - Add `.gitignore` (excluding `.gradle/`, `build/`, `local.properties`, `*.jks`, `.env`) and `.github/workflows/android-ci.yml` so GitHub Actions builds and uploads the debug APK on every push.
2. **Task `T1.2` — Core Domain Models & Environment Badge System (`~1 hour`):**
   - Define strongly typed Kotlin models for `DevicePlatform` (`ANDROID`, `WINDOWS`), `ExecutionEnvironmentType` (`TERMUX_HOST`, `DEBIAN_PROOT`, `WINDOWS_POWERSHELL`, `ANDROID_SYSTEM`), `ImpactLevel` (`READ_ONLY`, `CONFIG_CHANGE`, `HIGH_CAUTION`), and `VerificationState` (`VERIFIED`, `UNVERIFIED_DRAFT`, `AI_SUGGESTED`).
   - Implement `SecretGuardValidator` with unit tests verifying detection of private keys, GitHub PATs, API keys, and inline passwords.
3. **Task `T1.3` — Material 3 Theme, 4-Tab Navigation & Execution Context Card UI (`~1 hour`):**
   - Build the Material 3 theme tailored for high-contrast readability and distinct environment color accents.
   - Build the 4-tab navigation shell (`Recall`, `Library`, `Troubleshoot`, `Systems`) and the 7-part **Execution Context Card** composable.
4. **Task `T1.4` — Unit Tests, Build Verification & 5-Minute Owner Acceptance Check (`~0.5 hour`):**
   - Run `./gradlew testDebugUnitTest assembleDebug` and verify zero errors, passing unit tests, and a generated `app-debug.apk`.

---

## Delivery rules

- Follow the hierarchy: **Phase → Milestone → Goal → Task → Deliverable → Acceptance Criteria**.
- Never mark work complete without concrete evidence (passing test output, verified file, or owner sign-off).
- Keep user acceptance checks under 5 minutes whenever practical.
- Do not begin a later phase while its dependency or approval gate is unresolved.

