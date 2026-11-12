# Architecture Decision Log

Record decisions with date, context, options, rationale, consequences, and status.

## ADR 0001: Discovery before implementation

**Date:** 2026-11-12  
**Status:** Accepted  
**Decision:** Complete Phase 0 discovery and receive owner approval before implementing the Android application.  
**Reason:** Platform boundaries and the actual MVP are not yet validated.  
**Consequence:** App implementation remains blocked until the Phase 0 gate is passed.

## ADR 0002: Memory before autonomous execution

**Date:** 2026-11-12  
**Status:** Accepted  
**Decision:** Prioritize searchable technical memory and guided workflows before command execution.  
**Reason:** Memory can deliver value with substantially less security and integration risk.  
**Consequence:** Unrestricted shell execution is excluded from the MVP.

## ADR 0003: Treat Termux and Debian proot as separate environments

**Date:** 2026-11-12  
**Status:** Accepted  
**Decision:** Store and display Termux host and Debian proot as distinct execution targets.  
**Reason:** They have different filesystem, package, process, and environment boundaries.  
**Consequence:** Every command recipe must declare its target environment.

## ADR 0004: Native Android with Kotlin and Jetpack Compose

**Date:** 2026-11-12  
**Status:** Proposed (Awaiting Phase 0 Owner Sign-Off)  
**Context:** NEXUS needs an Android APK as its primary interface for a single owner who is an experienced UX/UI designer. Options considered: (1) Native Android with Kotlin and Jetpack Compose (Material 3), (2) Flutter, (3) React Native.  
**Decision:** Build NEXUS as a native Android application using **Kotlin** and **Jetpack Compose (Material 3)**.  
**Reason:** Native Kotlin + Compose provides first-class access to Android Keystore, Room, DataStore, Storage Access Framework, and future Termux `RUN_COMMAND` Intents without cross-platform bridge complexity, while giving fine-grained visual control over custom context badges and cards.  
**Consequence:** Single clean Kotlin codebase (`app/` module) built with Gradle Wrapper (`./gradlew`) and JDK 17.

## ADR 0005: Offline-First Local Storage with Room (SQLite + FTS4), DataStore, and SAF Export

**Date:** 2026-11-12  
**Status:** Proposed (Awaiting Phase 0 Owner Sign-Off)  
**Context:** NEXUS must remember devices, environments, recipes, workflows, and troubleshooting history reliably without cloud dependencies or vendor lock-in.  
**Decision:** Use **Room over SQLite with FTS4 full-text search** for structured domain records, **Jetpack Preferences DataStore** for lightweight UI state, and **JSON + Markdown export/import via Android Storage Access Framework (SAF)**.  
**Reason:** Works 100% offline, supports instant search across error messages and commands, requires zero storage permissions on modern Android, and guarantees the owner can inspect and back up all knowledge in plain human-readable files.  
**Consequence:** Schema migrations must be versioned and tested in Phase 2; exports automatically exclude any Keystore-held credentials.

## ADR 0006: Offline-First Memory with Pluggable `AiAssistantProvider` Interface

**Date:** 2026-11-12  
**Status:** Proposed (Awaiting Phase 0 Owner Sign-Off)  
**Context:** The Pixel 7 Pro has 12 GB RAM shared with Android, Termux, and Debian `proot`. Bundling a multi-gigabyte local LLM inside the MVP APK would bloat the app and risk triggering Android's Phantom Process Killer, while hardcoding a cloud AI would break offline reliability and raise privacy concerns.  
**Decision:** Make Phases 1–3 function **100% offline without requiring any AI model**, and introduce a replaceable `AiAssistantProvider` interface in Phase 4 with: (1) a default `OfflineRetrievalProvider` (deterministic local search + structured explanations), (2) an opt-in `RemoteGeminiProvider` (requiring pre-send secret redaction and explicit user Context Preview), and (3) a future `LocalLlmEndpointProvider` (for LAN/local OpenAI-compatible servers).  
**Reason:** Ensures NEXUS is fast, reliable, and useful even with zero internet or API keys, while supporting grounded AI diagnostics safely when desired.  
**Consequence:** All AI outputs are visually tagged `✨ AI Suggested — Unverified` and never executed automatically.

## ADR 0007: Guided Copy-and-Paste Handoff for MVP; Permissioned Termux Bridge in Phase 5–6

**Date:** 2026-11-12  
**Status:** Proposed (Awaiting Phase 0 Owner Sign-Off)  
**Context:** Android's UID/SELinux sandbox blocks an ordinary APK from directly running commands in Termux or Debian `proot`.  
**Decision:** Use **Guided Copy-and-Paste Handoff with Prompt Verification** in the MVP (Phases 1–4), and defer automated read-only inspection and allowlisted command execution to Phases 5–6 using either Termux's `com.termux.permission.RUN_COMMAND` Intent or localhost SSH (`127.0.0.1:8022`).  
**Reason:** Delivers immediate value with zero Termux configuration burden and zero execution attack surface during the MVP phases.  
**Consequence:** Every recipe card must clearly show the expected terminal prompt and environment entry command so manual handoff is error-free.

## ADR 0008: Defer Windows 11 Companion Agent to Phase 7 (Optional)

**Date:** 2026-11-12  
**Status:** Proposed (Awaiting Phase 0 Owner Sign-Off)  
**Context:** The owner already connects from Windows 11 to Termux via OpenSSH (`ssh -p 8022`). Building a custom background agent on Windows 11 early would require firewall changes, cross-OS pairing, and ongoing maintenance.  
**Decision:** Store Windows 11 device/environment profiles and Windows SSH recipes inside the NEXUS Android app in the MVP, and postpone any Windows software companion to Phase 7 (Optional).  
**Reason:** Avoids premature multi-OS engineering while still solving Windows-to-Termux SSH setup and troubleshooting inside NEXUS memory.  
**Consequence:** Windows commands are displayed clearly in NEXUS with `💻 Windows 11 · PowerShell` context badges.

## ADR 0009: Client-Side Secret Guard and Hardware-Backed Android Keystore

**Date:** 2026-11-12  
**Status:** Proposed (Awaiting Phase 0 Owner Sign-Off)  
**Context:** Technical notes and troubleshooting logs often contain accidental pastes of SSH private keys, API keys, or tokens.  
**Decision:** Implement a deterministic `SecretGuardValidator` that scans all user inputs, pasted outputs, exports, and AI prompts to block or redact secrets (`PRIVATE KEY`, `ghp_`, `github_pat_`, `AIza`, `sk-`, `Bearer`, password assignments), and store any future integration credentials exclusively in Android Keystore.  
**Reason:** Enforces the non-negotiable rule that secrets never enter ordinary notes, logs, AI context, or backups.  
**Consequence:** Attempting to save a raw key or password in a note or recipe triggers a clear, educational warning prompting the user to replace it with a safe placeholder (e.g., `<YOUR_SSH_KEY_PATH>`).

