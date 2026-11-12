# Milestone Register

**Last Updated:** 2026-11-12  
**Structure:** Phase → Milestone → Goal → Deliverable → Acceptance Criteria  

| ID | Phase | Milestone | Goal & Deliverable | Est. Effort | Actual Effort | Dependency | Status | % Done | Remaining Work | Key Risks | Tests & Evidence |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **M0.1** | 0 | **User workflow inventory** | Rank recurring workflows, pain points, and memory targets in `docs/DISCOVERY.md` | 0.5h | 0.4h | None | **Completed** | **100%** | None | Missing edge workflows | Documented 6 ranked workflows in `docs/DISCOVERY.md` §1 |
| **M0.2** | 0 | **Environment audit** | Inspect development VM and document user-reported Windows, Android, Termux, Debian context | 0.5h | 0.5h | M0.1 | **Completed** | **100%** | Optional physical device verification | Cloud VM cannot directly probe local Windows/phone | Read-only shell audit (`java`, `javac`, `git`, `ssh`, `df`, `free`) recorded in `docs/DISCOVERY.md` §2 |
| **M0.3** | 0 | **Integration feasibility** | Map Android UID sandbox, Termux host, Debian `proot`, and Windows SSH boundaries | 0.5h | 0.5h | M0.2 | **Completed** | **100%** | None | Confusing Termux host with Debian `proot` | 4-environment boundary map and SSH failure mode analysis in `docs/DISCOVERY.md` §3 |
| **M0.4** | 0 | **Architecture, UX & Security recommendation** | Compare options, design 4-tab UX & Context Card, define Secret Guard, and record ADRs | 1.0h | 0.8h | M0.1–M0.3 | **Completed** | **100%** | None | Over-engineering MVP | Updated `ARCHITECTURE.md`, `DECISIONS.md` (`ADR 0004`–`0009`), `SECURITY.md`, `DISCOVERY.md` §4–6 |
| **M0.5** | 0 | **Phase 0 approval gate** | Present Phase 0 report and receive explicit owner approval for Phase 1 | Owner review (~5m) | 0.1h | M0.4 | **Completed** | **100%** | None | None | Owner approved Phase 0 and authorized Phase 1 (`2026-11-12`) |
| **M1.1** | 1 | **Android project foundation & UX shell** | Buildable Kotlin/Compose app shell, 4-tab navigation, Execution Context Card UI, Secret Guard unit tests, and CI APK build | 3–4h | 1.0h | M0.5 | **Completed** | **100%** | None | None | Verified `./gradlew testDebugUnitTest assembleDebug` passed (5/5 JUnit tests) and generated `app-debug.apk` |
| **M2.1** | 2 | **Local memory schema & starter pack** | Room SQLite + FTS4 database, SAF JSON/Markdown export/import, and persistent editable memory | 4–5h | 0h | M1.1 | **Ready (Next Up)** | **0%** | Tasks `T2.1`–`T2.4` | FTS4 tokenizer edge cases on symbols/flags | Target: Room DAO + export/import unit tests passing |
| **M3.1** | 3 | **Recipe search & resumable guided workflows** | Instant environment-filtered search, verification badges, troubleshooter UI, and stateful step-by-step workflow runner | 4–5h | 0h | M2.1 | **Blocked** | **0%** | Tasks `T3.1`–`T3.4` | Workflow state persistence across process death | Target: ViewModel state & workflow persistence tests passing |
| **M4.1** | 4 | **Contextual grounded assistant** | `AiAssistantProvider` abstraction, offline retrieval responder, opt-in Gemini redaction + Context Preview dialog | 3–4h | 0h | M3.1 | **Blocked** | **0%** | Tasks `T4.1`–`T4.4` | Hallucinated flags or prompt injection | Target: Redaction & untrusted-output safety tests passing |
| **M5.1** | 5 | **Read-only device inspection** | Permissioned Termux `RUN_COMMAND` or localhost SSH read-only status checks | 4–6h | 0h | M4.1 + Security review | **Blocked (Post-MVP)** | **0%** | Phase 5 scope | Android Doze / Termux permission setup | Target: Timeout, disconnect, and read-only enforcement tests |
| **M6.1** | 6 | **Approved operation runner** | Typed allowlisted operations with per-action approval, timeouts, redaction, and audit log | 5–7h | 0h | M5.1 + Explicit approval | **Blocked (Post-MVP)** | **0%** | Phase 6 scope | Command injection if parameters unvalidated | Target: Parameter validator & allowlist rejection tests |
| **M7.1** | 7 | **Windows companion evaluation** | Optional Windows 11 bridge prototype if manual SSH handoff proves insufficient | 6–8h | 0h | M6.1 | **Optional / Deferred** | **0%** | Evaluate necessity after Phase 6 | Cross-OS firewall and pairing complexity | Deferred per `ADR 0008` |
| **M8.1** | 8 | **Personal APK release** | Tested installable APK build, backup/restore verification, and 5-minute owner playbook | 2–3h | 0h | M4.1 (Core MVP) | **Blocked** | **0%** | Release packaging & docs | Keystore signing backup | Target: Clean install & restore check on Pixel 7 Pro |
| **M9.1** | 9 | **Commercial feasibility review** | Evidence-based go/no-go after personal utility is proven | TBD | 0h | M8.1 | **Optional** | **0%** | Post-MVP evaluation | Premature multi-tenant complexity | Deferred |

---

## Completion policy

A milestone is complete only when its deliverable exists, acceptance criteria pass, evidence is recorded, and the owner accepts any user-facing or security-sensitive behavior. User acceptance checks are designed to take **5 minutes or less**.

Do not mark blocked work as in progress merely to improve progress statistics.

