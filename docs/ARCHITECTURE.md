# Architecture Proposal & Validated Design

**Status:** Phase 0 Validated Recommendation — Awaiting Owner Approval (`2026-11-12`)

## 1. High-level structure

1. **Android Application (Kotlin + Jetpack Compose):** Primary user interface and local experience designed for high visual clarity, explicit environment badges, and zero terminal ambiguity.
2. **Local Knowledge Store (Room over SQLite + FTS4):** Offline-first relational database storing devices, execution environments, connection profiles (metadata only), command recipes, troubleshooting cases, guided workflows, and audit history.
3. **Secret Guard & Preference Layer (DataStore + Android Keystore):** Jetpack Preferences DataStore for UI state and non-sensitive settings; Android Keystore (`EncryptedSharedPreferences` / Tink AES-GCM) for any future API keys or SSH keypairs; `SecretGuardValidator` to block raw secrets from entering ordinary notes.
4. **Context & Retrieval Layer:** Deterministic local full-text search (FTS4) and environment-scoped context builder that selects relevant verified knowledge before any AI model is consulted.
5. **Pluggable AI Provider Interface (`AiAssistantProvider`):** Allows offline deterministic guidance by default, optional remote Gemini API integration (with pre-send secret redaction and user Context Preview), or future local LLM endpoints without rewriting product logic.
6. **Optional Device Bridge (Phase 5–6):** Permissioned Termux `RUN_COMMAND` Intent bridge or localhost/LAN SSH runner (`127.0.0.1:8022`) for read-only inspection and typed allowlisted operations.
7. **Optional Windows Companion (Phase 7):** Deferred unless cross-network Windows automation proves necessary after Phases 1–6.

---

## 2. Recommended technology stack

| Area | Recommended Choice | Validation Rationale |
| --- | --- | --- |
| **Language & UI** | **Kotlin 2.x + Jetpack Compose (Material 3)** | Native Android toolkit with zero cross-platform bridge overhead, full control over custom typography/badges, and direct access to Android Intents and Keystore. |
| **Build System** | **Gradle Wrapper (`./gradlew`) + AGP 8.x + JDK 17** | OpenJDK `17.0.21` is verified installed in the Antigravity VM; Gradle Wrapper ensures reproducible builds in both the VM and GitHub Actions CI. |
| **Local Database** | **Room (SQLite) + FTS4 Full-Text Search** | Fast, offline-first structured storage with compile-time SQL verification and sub-millisecond keyword search across recipes and error logs. |
| **Preferences** | **Jetpack Preferences DataStore** | Coroutine-safe storage for active environment filter, theme mode, and resumable workflow pointers. |
| **Secret Storage** | **Android Keystore (Hardware-Backed on Pixel 7 Pro)** | Keeps future AI API keys or SSH keys isolated from the SQLite database and exported backups. |
| **AI Integration** | **`AiAssistantProvider` Kotlin Interface** | Cleanly decouples UI from AI providers; supports 100% offline use in Phases 1–3 and safe opt-in AI in Phase 4. |
| **Backup & Export** | **Kotlinx Serialization (JSON + Markdown Bundle via SAF)** | Uses Android Storage Access Framework (system file picker) requiring zero broad storage permissions and preventing vendor lock-in. |
| **Device Bridge (Later)** | **Guided Handoff (MVP) -> Termux `RUN_COMMAND` / SSH (Phase 5+)** | Delivers immediate personal value in MVP without background daemon failures or open network ports. |

---

## 3. Validated data model (Room SQLite entities)

- **`DeviceEntity`**: `id`, `name` (e.g., `Google Pixel 7 Pro`, `Windows 11 Laptop`), `platformType` (`ANDROID`, `WINDOWS`), `model`, `ramGb`, `ownershipLabel`, `verificationStatus`, `updatedAt`.
- **`EnvironmentEntity`**: `id`, `deviceId` (foreign key), `name` (e.g., `Termux Host Shell`, `Debian proot`, `Windows PowerShell`), `environmentType` (`TERMUX_HOST`, `DEBIAN_PROOT`, `WINDOWS_SHELL`, `ANDROID_OS`), `osRelease`, `shellName`, `packageManager` (`pkg`, `apt`, `winget`), `promptIndicator` (e.g., `~ $`, `root@localhost:~#`), `entryCommand` (e.g., `proot-distro login debian`), `filesystemBoundaryNote`.
- **`ConnectionProfileEntity`**: `id`, `sourceEnvironmentId`, `targetEnvironmentId`, `protocol` (`SSH`, `ADB_FORWARD`, `TERMUX_INTENT`), `hostPlaceholder`, `port` (e.g., `8022`), `authMethodLabel` (e.g., `SSH Public Key` — never stores raw private keys or passwords), `prerequisitesNote`, `trustState`.
- **`RecipeEntity` (+ `RecipeFtsEntity`)**: `id`, `title`, `purposePlainEnglish`, `targetDeviceId`, `targetEnvironmentId`, `commandSyntax`, `parameterBreakdownJson`, `expectedOutput`, `prerequisiteCheck`, `rollbackHint`, `impactLevel` (`READ_ONLY`, `CONFIG_CHANGE`, `HIGH_CAUTION`), `verificationStatus` (`VERIFIED`, `UNVERIFIED_DRAFT`, `AI_SUGGESTED`), `lastVerifiedAt`.
- **`TroubleshootingCaseEntity`**: `id`, `symptomTitle`, `errorTextPattern`, `environmentId`, `plainEnglishDiagnosis`, `readOnlyCheckCommand`, `verifiedFixRecipeId`, `evidenceNote`, `status` (`RESOLVED_VERIFIED`, `INVESTIGATING`).
- **`WorkflowEntity` & `WorkflowStepEntity`**: Ordered multi-step procedures (`id`, `title`, `description`, `currentStepIndex`, `isCompleted`, `lastUpdatedAt`) where each step links to a target environment, command, expected output, and completion state that persists across app restarts.
- **`AuditLogEntity`**: `id`, `timestamp`, `actionType` (`RECIPE_COPIED`, `WORKFLOW_STEP_COMPLETED`, `RECIPE_VERIFIED`, `EXPORT_CREATED`, `SECRET_BLOCKED`, or future `OPERATION_APPROVED`), `targetEnvironmentId`, `summary`, `redactedOutput`.

---

## 4. Information architecture and UX screens

Designed specifically for a visual/UX designer owner so technical boundaries are immediately visible:

1. **Recall (Home & Context Switcher):**
   - Persistent top **Environment Pill Bar** (`All` | `📱 Pixel 7 Pro · Termux` | `📱 Pixel 7 Pro · Debian proot` | `💻 Windows 11`).
   - Instant full-text search bar across recipes, symptoms, and workflows.
   - **Active Workflow Resume Banner** (if a multi-step guide is in progress).
   - **Pinned Quick Recipes** (e.g., *Start Termux `sshd`*, *Check Phone Wi-Fi IP*, *Enter Debian `proot`*, *Connect from Windows SSH*).
2. **Library (Recipes & Guided Workflows):**
   - Every command renders inside a standardized **Execution Context Card** displaying:
     1. **Target Device Pill** (`Pixel 7 Pro` vs `Windows 11`)
     2. **Target Environment Badge** (`🟢 Termux Host` vs `🟣 Debian proot` vs `🔵 Windows PowerShell`)
     3. **Where-to-Run & Prompt Verification Strip** (`Expected prompt: ~ $` + how to switch shells)
     4. **Plain-English Purpose & Flag Breakdown**
     5. **Expected Output Preview**
     6. **Verification Status Badge** (`✅ Verified` | `📝 Draft` | `✨ AI Suggested — Unverified`)
     7. **Impact Badge** (`🟢 Read-Only` | `🟡 Config Change` | `🔴 High Caution`)
3. **Troubleshoot (Error Diagnosis & Grounded Assistant):**
   - Structured `Symptom -> Plain-English Cause -> Safe Check -> Verified Fix` cards.
   - Phase 4 **Grounded Assistant** view that retrieves matching local recipes/cases first, displays citations to saved memory, and previews redacted context before any external AI query.
4. **Systems (Devices, Environments, Backup & Audit):**
   - Visual hierarchy of devices and nested environments (`Pixel 7 Pro` -> `Termux` -> `Debian proot`), boundary comparison table (`pkg` vs `apt`, ports, paths), portable JSON/Markdown export/import, and Audit Log.

---

## 5. Critical platform boundary

An ordinary Android application runs inside its own Linux UID and SELinux sandbox (`/data/data/com.zakshub.nexus`). It cannot directly invoke commands inside Termux (`/data/data/com.termux`) or Debian `proot`. Furthermore, Termux host and Debian `proot` are separate execution contexts with different package managers (`pkg` vs `apt`) and filesystem roots.
- **Phases 1–4 (MVP):** Use **Guided Handoff** (one-tap copy with environment prompt verification and optional output paste).
- **Phases 5–6 (Later):** Optional permissioned integration via Termux `com.termux.permission.RUN_COMMAND` Intent or authenticated localhost SSH (`127.0.0.1:8022`) using typed allowlisted operations.

---

## 6. Security architecture requirements

- No unrestricted shell interface in any phase.
- No model-generated command may execute directly.
- Client-side `SecretGuardValidator` prevents saving raw passwords, SSH private keys, or API tokens in notes or recipes.
- Zero network permissions (`INTERNET`) required in Phases 1–3.
- Every future operation (Phase 5+) must be represented by a typed, allowlisted action with parameter validation, explicit per-action approval, timeouts, output size limits, secret redaction, audit logging, and rollback guidance.
- Use Android Keystore for credentials when Phase 4+ integrations are enabled.

---

## 7. AI design requirements

- Core product (Phases 1–3) functions 100% offline without AI.
- Treat all model responses as untrusted input.
- Retrieve only relevant, secret-redacted user context and show a **Context Preview** before sending to any remote model.
- Visually separate saved `✅ Verified` recipes from `✨ AI Suggested — Unverified` cards.
- Never imply a command was executed unless verified execution evidence confirms it.
- Keep provider credentials outside source control and exported backups.

---

## 8. Architecture decisions resolved in Phase 0

See [docs/DECISIONS.md](DECISIONS.md) for the full Architecture Decision Records (`ADR 0001` through `ADR 0009`):
- **Framework:** Native Android with Kotlin + Jetpack Compose (`ADR 0004`).
- **Storage & Backup:** Room (SQLite + FTS4) + DataStore + SAF JSON/Markdown Export (`ADR 0005`).
- **AI Strategy:** Offline-first memory + replaceable `AiAssistantProvider` interface (`ADR 0006`).
- **Termux & Windows Integration:** Guided Handoff for MVP; permissioned Termux Intent / SSH deferred to Phase 5–6; Windows companion deferred to Phase 7 (`ADR 0007`, `ADR 0008`).
- **Secret Protection & Pairing:** Client-side Secret Guard + Android Keystore (`ADR 0009`).

