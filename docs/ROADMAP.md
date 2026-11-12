# Roadmap

**Current status:** Phase 0 Discovery completed (`88%` — Milestones `M0.1` to `M0.4` complete; `M0.5` awaiting owner approval). No application code has been written yet.

| Phase | Name | Goal | Est. Effort | Status | Completion |
| --- | --- | --- | --- | --- | --- |
| **0** | **Discovery and validation** | Verify environment, use cases, boundaries, UX, security, and architecture | 2.5 hours | **Awaiting Owner Sign-Off (`M0.5`)** | **88%** |
| **1** | **Android foundation & UX shell** | Create Kotlin/Compose app shell, 4-tab navigation, Execution Context Card UI, unit tests, and CI build pipeline | 3–4 hours | **Ready — Blocked by Phase 0 Approval** | 0% |
| **2** | **Technical memory & starter pack** | Room SQLite + FTS4 database, Secret Guard, JSON/MD export, and pre-seeded Pixel 7 Pro / Termux / Debian / Windows 11 memory | 4–5 hours | Planned | 0% |
| **3** | **Search & guided workflows** | Instant environment-filtered search, recipe verification flows, and resumable step-by-step workflows | 4–5 hours | Planned | 0% |
| **4** | **Contextual AI assistant** | Pluggable `AiAssistantProvider` with offline retrieval, opt-in Gemini redaction + Context Preview, and citations | 3–4 hours | Planned | 0% |
| **5** | **Read-only inspection** | Permissioned Termux Intent or localhost SSH read-only environment health checks | 4–6 hours | Postponed (Post-MVP) | 0% |
| **6** | **Approved operations** | Typed allowlisted operations with per-action approval, timeouts, redaction, and audit trail | 5–7 hours | Postponed (Post-MVP) | 0% |
| **7** | **Windows companion** | Optional Windows 11 bridge if manual SSH handoff proves insufficient | 6–8 hours | Optional / Deferred | 0% |
| **8** | **Personal release** | Signed/installable personal APK, full backup/restore verification, and owner playbook | 2–3 hours | Planned | 0% |
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

