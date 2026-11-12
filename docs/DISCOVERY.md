# Phase 0 Discovery

**Status:** Completed — Awaiting Owner Approval for Phase 1  
**Date Completed:** 2026-11-12  
**Purpose:** Validate the problem, environment, integration boundaries, user experience, security model, architecture, and MVP before implementation.

---

## 1. User workflow discovery

The owner is an experienced visual and UX/UI designer working across a Windows 11 laptop and a Google Pixel 7 Pro running Android, Termux, and Debian via `proot`. Below is the prioritized ranking of recurring workflows based on frequency, time lost when context is forgotten, and failure impact:

| Rank | Workflow | Frequency | Time Lost per Incident | Failure Impact | Current Workaround | What NEXUS Must Remember |
| --- | --- | --- | --- | --- | --- | --- |
| **1** | **Distinguishing Termux host from Debian `proot` and running commands in the right shell** | Daily / Every session | 10–30 min | High — running `apt` vs `pkg`, editing the wrong filesystem path, or installing packages into the wrong environment | Trial-and-error, scrolling terminal history, re-asking chat assistants | Exact environment boundaries, shell prompts, entry/exit commands (`proot-distro login debian`), package manager rules (`pkg` in Termux vs `apt` in Debian), and target environment badge on every command |
| **2** | **Windows 11 to Termux SSH connection and troubleshooting** | Daily | 15–45 min | High — blocks remote keyboard/terminal workflow from Windows laptop to phone | Searching notes, checking IP manually, restarting Termux | Termux SSH port (`8022`), `sshd` start/stop commands, `termux-wake-lock`, finding current phone Wi-Fi IP (`ip -4 addr show wlan0`), and step-by-step diagnosis when `Connection refused` or timeout occurs |
| **3** | **Recalling verified Linux commands, flags, and multi-step setup procedures** | Several times a week | 15–30 min | Medium–High — wrong flags or skipped prerequisites break tools | Scattered chat logs and ad-hoc text notes | Plain-English explanation of what the command does, every parameter's meaning, expected output, safety level (read-only vs state-changing), and whether it was verified on the owner's actual device |
| **4** | **Git and GitHub setup, authentication, and recovery across environments** | Weekly | 15–40 min | Medium–High — failed pushes, detached HEADs, or credential confusion between Windows, Termux, and Debian | Searching online guides or re-running setup commands | Verified Git config per environment, branch/status check recipes, safe recovery workflows, and credential storage method (without ever storing raw tokens/keys in notes) |
| **5** | **Antigravity and AI-assisted development workflow continuity** | Several times a week | 10–25 min | Medium — losing track of environment state and project milestones across sessions | Reading repo docs manually | Project conventions, repository paths, verification commands, and environment inventory |
| **6** | **Repeating device configuration and development environment health checks** | Weekly / After device reboot or update | 10–30 min | Medium — services silently stopped by Android battery optimization or reboot | Re-discovering which services need restarting | Post-reboot startup checklist (Termux wake-lock, `sshd`, Debian `proot` entry, network check) and verified software versions |

---

## 2. Environment audit

> **Important Distinction (Verified Facts vs. User-Reported Context):**  
> Antigravity executes inside an isolated Linux cloud virtual machine (`Debian 12 bookworm x86_64`) connected to the `https://github.com/zakshub/nexus` repository. Antigravity **does not** have direct network or shell access to the owner's physical Windows 11 laptop or Google Pixel 7 Pro. Therefore, this audit strictly separates **Directly Verified Facts** (inspected via read-only commands in the Antigravity workspace environment) from **User-Reported Starting Context** (which relates to the owner's physical devices).

### 2.1 Directly Verified Facts: Antigravity Cloud Build & Development VM

Inspected on `2026-11-12` via non-destructive read-only shell checks in `/home/antigravity/zakshub/nexus`:

| Component | Availability | Verified Version / Details | Evidence Command |
| --- | --- | --- | --- |
| **Operating System** | **Installed (Verified)** | Debian GNU/Linux 12 (bookworm) `x86_64`, Kernel `6.12.105+` | `uname -a && cat /etc/os-release` |
| **CPU & Memory** | **Available (Verified)** | 2 vCPU (`Intel(R) Xeon(R) Platinum 8481C`), 7.8 GiB RAM (7.1 GiB available) | `lscpu && free -h` |
| **Disk Storage** | **Available (Verified)** | `/home/antigravity`: 19 GB total, **19 GB free (3% used)**; `/tmp`: 42 GB free | `df -h /home/antigravity /tmp` |
| **Java / JDK** | **Installed (Verified)** | **OpenJDK 17.0.21** (`2026-10-20`, build `17.0.21+9-1-deb12u1-Debian`) at `/usr/bin/java` and `/usr/bin/javac` (`/usr/lib/jvm/java-17-openjdk-amd64`) | `java --version && javac --version` |
| **Git** | **Installed (Verified)** | `git version 2.39.5` at `/usr/bin/git` (Author: `ZAK`, Remote: `origin https://github.com/zakshub/nexus.git`) | `git --version && git remote -v` |
| **GitHub CLI (`gh`)** | **Installed with Sandbox Quirk (Verified)** | Launcher at `/opt/antigravity/bin/gh` (pinned `v2.103.3`), GitHub plugin connected via proxy. Note: First-run tar extraction into `/shared/user/.gemini` fails on `utime` permission, while `git` push/fetch works natively through the credential proxy. | `/opt/antigravity/bin/gh --version` |
| **OpenSSH Client** | **Installed (Verified)** | `OpenSSH_9.2p1 Debian-2+deb12u10, OpenSSL 3.0.22` at `/usr/bin/ssh` | `ssh -V` |
| **Node.js & npm** | **Installed (Verified)** | Node `v24.22.2` (`/usr/local/bin/node`), npm `11.21.2` | `node --version && npm --version` |
| **Python** | **Installed (Verified)** | `Python 3.11.2` (`/usr/bin/python3`) | `python3 --version` |
| **Android SDK (`sdkmanager`)** | **Not Installed (Verified)** | Not present on `PATH`; no `ANDROID_HOME` or `/opt/android-sdk` directory | `command -v sdkmanager` |
| **Gradle (`gradle`)** | **Not Installed Globally (Verified)** | Not on `PATH`. Standard Android projects use a project-local Gradle Wrapper (`./gradlew`) | `command -v gradle` |
| **Kotlin CLI (`kotlinc`)** | **Not Installed Standalone (Verified)** | Not on `PATH`. Android builds resolve the Kotlin compiler automatically via the Gradle plugin | `command -v kotlinc` |
| **ADB (`adb`)** | **Not Installed (Verified)** | Not on `PATH`; VM has no physical USB or local Wi-Fi link to the Pixel 7 Pro | `command -v adb` |
| **Android Studio** | **Not Installed (Verified)** | Headless Linux VM; no desktop IDE installed | `command -v studio` |

**Build Feasibility Impact:** Because OpenJDK 17 (`17.0.21`) and 19 GB of writable user-space storage are already available in `/home/antigravity`, the Antigravity VM can compile and test the Android app in Phase 1 once the owner authorizes downloading the Android command-line SDK (`cmdline-tools`, `platforms;android-35`, `build-tools;35.0.0`, ~1.5 GB) into user space (`/home/antigravity/android-sdk`) and/or configuring a GitHub Actions workflow to build downloadable APKs automatically on every commit.

### 2.2 User-Reported Context (Unverified Directly — Owner's Physical Devices)

| Environment | User-Reported Details | Verification Status | What Remains Unverified |
| --- | --- | --- | --- |
| **Windows 11 Laptop** | Windows 11, Windows SSH client, Git, GitHub, Antigravity | **User-Reported (Unverified)** | Exact Windows 11 build, PowerShell version, whether JDK / Android Studio / Android SDK / ADB are installed locally on Windows |
| **Google Pixel 7 Pro (Android)** | Pixel 7 Pro with 12 GB RAM running Android | **User-Reported (Unverified)** | Exact Android OS version (e.g., Android 14 / 15 / 16), available internal storage, battery optimization status for Termux |
| **Termux (on Pixel 7 Pro)** | Termux installed with OpenSSH server (`sshd`) and `proot` | **User-Reported (Unverified)** | Termux installation source (F-Droid vs GitHub release — critical because Play Store Termux is deprecated), OpenSSH version, active SSH port (standard Termux default is `8022`), whether `termux-api` or `allow-external-apps` is enabled |
| **Debian `proot` (inside Termux)** | Debian Linux running via `proot` inside Termux | **User-Reported (Unverified)** | Debian release (e.g., Bookworm 12), how it is launched (`proot-distro login debian` vs custom script), installed packages |

*(Note: With the recommended architecture below, the owner **does not** need to install Android Studio or the Android SDK on the Windows 11 laptop unless desired, because builds and automated tests can run in the Antigravity environment and GitHub Actions, producing an installable `.apk` directly.)*

---

## 3. Integration feasibility and technical boundaries

### 3.1 The 4 Distinct Execution Environments

A core requirement of NEXUS is preventing the user from running the right command in the wrong place. There are four separate environments in the owner's ecosystem:

```
+-----------------------------------------------------------------------------------+
| 1. WINDOWS 11 LAPTOP (Desktop OS)                                                 |
|    - Shells: PowerShell / Command Prompt / Git Bash                               |
|    - Role: Primary workstation, runs `ssh -p 8022 <phone-ip>` to reach Termux     |
+-----------------------------------------+-----------------------------------------+
                                          |
                     Local Wi-Fi LAN (SSH over TCP, usually port 8022)
                     or USB cable (`adb forward tcp:8022 tcp:8022`)
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| GOOGLE PIXEL 7 PRO (Android OS, 12 GB RAM, SELinux + Per-App UID Sandboxes)       |
|                                                                                   |
|  +----------------------------------+       +----------------------------------+  |
|  | 2. NEXUS ANDROID APK             |       | 3. TERMUX APK (Host Environment) |  |
|  |    - App UID: e.g., u0_a310      |  =/=> |    - App UID: e.g., u0_a245      |  |
|  |    - Private storage:            |Blocked|    - Prefix: $PREFIX (/usr)      |  |
|  |      /data/data/com.zakshub.nexus|by OS  |    - Package manager: `pkg`      |  |
|  |    - Local Room SQLite DB        |Sandbox|    - Runs `sshd` daemon          |  |
|  |    - Android Keystore secrets    |       |                                  |  |
|  +----------------------------------+       |  +----------------------------+  |  |
|                                             |  | 4. DEBIAN PROOT            |  |  |
|                                             |  |    (Inside Termux)         |  |  |
|                                             |  |    - Simulated root (`/`)  |  |  |
|                                             |  |    - Package mgr: `apt`    |  |  |
|                                             |  |    - Separate `/etc`, `/usr`|  |  |
|                                             |  +----------------------------+  |  |
|                                             +----------------------------------+  |
+-----------------------------------------------------------------------------------+
```

### 3.2 Why an Ordinary Android APK Cannot Directly Run Termux or Debian Commands

1. **Android Application Sandbox (Linux UID + SELinux Isolation):**  
   Android assigns every installed APK its own unique Linux user ID (UID) and isolated data directory (`/data/data/<package>`). By operating system enforcement, the NEXUS APK **cannot** read Termux's home folder, inspect Termux's running processes, or spawn a shell inside Termux's `/data/data/com.termux/files/usr/bin` directory. If NEXUS calls `Runtime.getRuntime().exec("sh")`, it only gets a stripped-down Android toybox shell confined to NEXUS's own empty sandbox — where neither `pkg`, `apt`, `ssh`, `git`, nor `proot` exist.
2. **Termux vs. Debian `proot` Boundary:**  
   Termux is an Android app providing a Linux-like userland compiled specifically for Android's Bionic libc (`$PREFIX = /data/data/com.termux/files/usr`), managed with `pkg`. Debian `proot` runs *inside* Termux using `ptrace` system call translation to fake a standard Debian root filesystem (`/`) using GNU libc (`glibc`) and `apt`.
   - A command meant for Debian (e.g., `apt update && apt install build-essential`) run in raw Termux either fails or modifies Termux packages instead of Debian.
   - A command meant for Termux (e.g., `termux-wake-lock` or `sshd`) run inside Debian `proot` often fails because Termux's Android IPC sockets and environment variables are not mapped the same way inside `proot`.

### 3.3 How Windows 11 Connects to Termux `sshd` and Common Failure Modes

- **Connection Mechanism:** Termux runs an unprivileged OpenSSH server (`sshd`). Because non-root Android apps cannot bind to TCP ports below `1024`, Termux `sshd` listens on port **`8022`** by default (not port `22`). On Windows 11, the user connects via `ssh -p 8022 <phone-local-ip>` (or `ssh <alias>` if configured in `C:\Users\<name>\.ssh\config`). Note: Termux ignores the username in `ssh user@ip` and always logs into the single Termux app user, landing in the **Termux host shell** (not Debian `proot`).
- **Network Conditions Required:**
  1. Both Windows 11 and Pixel 7 Pro must be on the same local Wi-Fi subnet without "AP Client Isolation" (guest Wi-Fi networks block device-to-device traffic), **OR** connected via Mobile Hotspot, **OR** connected via USB with `adb forward tcp:8022 tcp:8022`.
  2. Windows Firewall must allow outbound TCP on port `8022` (usually allowed by default).
- **What Happens When the Phone Sleeps or Network Changes:**
  1. **Android Doze & Background Freezing:** When the Pixel 7 Pro screen turns off, Android throttles Wi-Fi and freezes background apps, causing SSH sessions to lag or drop unless `termux-wake-lock` is active and Termux battery usage is set to **Unrestricted** in Android Settings.
  2. **Android 12+ Phantom Process Killer:** Android limits background child processes across the system, which can abruptly terminate `proot` or `sshd` sessions under memory pressure (even on a 12 GB RAM Pixel 7 Pro if many apps run).
  3. **Changing Local IP Address:** Reconnecting to Wi-Fi or switching networks can assign a new DHCP IP address (e.g., `192.168.1.42` -> `192.168.1.89`), breaking saved SSH commands unless checked via `ip -4 addr show wlan0` in Termux.

### 3.4 Realistic Integration Mechanisms Between NEXUS and Termux / Windows

| Integration Mechanism | How It Works | What It Can Do | User Effort / Setup Required | Recommended Phase |
| --- | --- | --- | --- | --- |
| **1. Guided Copy-and-Paste Handoff + Output Paste** | NEXUS shows the exact command, target device, target shell prompt, plain-English explanation, and expected output. One-tap copies command; user runs it in Termux or Windows SSH and optionally pastes output back. | 100% of memory, lookup, explanation, and step-by-step guidance across all 4 environments (Windows, Android, Termux, Debian `proot`). | **Zero setup.** Works out of the box with zero security surface. | **MVP (Phases 1–4)** |
| **2. Termux `RUN_COMMAND` Intent Bridge** | Android APK requests `com.termux.permission.RUN_COMMAND` and sends an explicit `RUN_COMMAND_SERVICE` Intent to Termux for an allowlisted script, receiving stdout/stderr/exit code via `PendingIntent`. | Can run allowlisted read-only checks or approved Termux/Debian helper scripts directly on the phone. | Requires user to set `allow-external-apps = true` in `~/.termux/termux.properties` and grant the Android permission to NEXUS. | **Phase 5–6 (Optional Local Phone Bridge)** |
| **3. Localhost / LAN Authenticated SSH Bridge** | NEXUS uses an embedded SSH client with an Android Keystore keypair to connect to `127.0.0.1:8022` (Termux on the same phone) or Windows SSH after explicit user approval. | Read-only environment inspection (e.g., checking if `sshd` is running, disk space, Debian version) and allowlisted operations. | Requires adding NEXUS's public key to Termux `~/.ssh/authorized_keys` and `sshd` running in Termux. | **Phase 5–6 (Alternative / Cross-Device Bridge)** |

---

## 4. Architecture comparison

### 4.1 Application Framework & Architecture Options

| Option | User Effort | Reliability | Security | Maintenance | Offline Support | Implementation Cost | Future Flexibility | Verdict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Option A: Native Android (Kotlin + Jetpack Compose + Room + DataStore) with Guided Handoff first** | Very Low (install APK, zero server setup) | Very High (no daemon crashes or network dependencies) | Very High (local sandbox, Android Keystore, no open ports) | Low (single standard Kotlin codebase) | **100% Offline** for core memory, search, and workflows | Low–Medium | High (natively supports Termux Intents, Keystore, and local/remote AI later) | **RECOMMENDED for MVP** |
| **Option B: Cross-Platform App (Flutter or React Native)** | Low | High | High | Medium–High (extra runtime + native bridges for Android Intents/Keystore) | 100% Offline | Medium | Medium–High (useful only if iOS/desktop UI parity is needed immediately) | **Not Recommended** — adds dependency complexity for a single-user Android-first tool |
| **Option C: Android App + Mandatory Termux Background Companion Service in MVP** | High (must configure daemon, keys, wake-locks before using app) | Low–Medium (fails whenever `sshd`/daemon sleeps or stops) | Medium (requires local IPC/socket auth from day one) | High (two components to debug) | Partial (breaks when daemon is stopped) | High | High | **Postpone to Phase 5–6** — violates "Memory before autonomy" principle |
| **Option D: Android App + Windows 11 Companion Agent in MVP** | Very High (install & pair Windows service + firewall rules) | Low across network changes | Low–Medium (cross-network attack surface) | Very High (two OS targets) | Low | Very High | Medium | **Postpone to Phase 7 (Optional)** |

---

## 5. UX and Information Architecture for a Non-Technical Experience

Because the owner is a UX/UI and graphics designer, NEXUS must never look or behave like a raw terminal log or a generic AI chat window. Every screen must communicate context visually and immediately.

### 5.1 The "Execution Context Card" Visual Standard

Whenever NEXUS displays a command recipe or workflow step, it renders an **Execution Context Card** containing seven mandatory visual elements:

1. **Device Pill:** High-contrast pill with icon (`📱 Pixel 7 Pro` or `💻 Windows 11 Laptop`).
2. **Environment Badge (Color-Coded & Never Merged):**
   - `🟢 Termux (Host Shell)` — Android Bionic userland (`pkg`)
   - `🟣 Debian (proot inside Termux)` — Debian Linux userland (`apt`)
   - `🔵 Windows 11 (PowerShell / SSH Client)`
   - `⚪ Android System Settings`
3. **"Where to Run & How to Verify You Are There" Strip:**  
   Shows the expected terminal prompt pattern (e.g., `~ $` in Termux vs `root@localhost:~#` in Debian `proot`) and a 1-tap expandable hint (*"Not in Debian yet? Run `proot-distro login debian` inside Termux first"*).
4. **Plain-English Explanation:**  
   A clear sentence explaining **what the command does** before showing the syntax, plus an expandable breakdown of each flag (e.g., `-p 8022` = *"Uses Termux's SSH port 8022"*).
5. **Expected Result Box:**  
   Describes what success looks like in plain English so the user immediately knows whether the command worked.
6. **Verification Trust Badge:**
   - `✅ Verified on My Setup` (green badge with last-verified date)
   - `📝 Saved Draft — Unverified` (neutral amber badge)
   - `✨ AI Suggestion — Unverified` (distinct dashed outline + warning callout; requires explicit user confirmation before promotion to a saved recipe)
7. **Safety / Impact Badge:**
   - `🟢 Read-Only (Safe Check)` — inspects status only; changes nothing.
   - `🟡 Changes Configuration` — installs packages or edits settings; includes undo/rollback note.
   - `🔴 High Caution` — deletes files, stops services, or resets state; requires an explicit warning acknowledgment before copying or running.

### 5.2 Initial Information Architecture & Main Screens

NEXUS uses a clean 4-tab bottom navigation structure (Material 3):

1. **Tab 1: Recall (Home & Quick Context Switcher)**
   - Top **Context Filter Bar**: Quickly scope the entire view to `All`, `Pixel 7 Pro · Termux`, `Pixel 7 Pro · Debian proot`, or `Windows 11`.
   - **Global Search Bar**: Instant full-text search across commands, symptoms, errors, and notes.
   - **Resume Active Workflow Card**: If a multi-step guide is in progress (e.g., *Reconnect Windows SSH to Termux*), it stays pinned at the top with current step progress (`Step 2 of 4`) even if the app is closed and reopened.
   - **Pinned Daily Recipes**: One-tap access to the owner's most frequent verified commands.
2. **Tab 2: Library (Recipes & Guided Workflows)**
   - **Recipes View**: Structured cards of verified commands organized by environment and task category (SSH, Package Management, Git, Proot, System Checks).
   - **Workflows View**: Interactive step-by-step checklists where each step has prerequisites, the command card, expected output, a "Mark Step Complete" toggle, and "What if this step failed?" branching hints.
3. **Tab 3: Troubleshoot (Symptom -> Diagnosis -> Verified Fix + Grounded Assistant)**
   - **Troubleshooting Cases**: Searchable cards structured as *Error / Symptom* -> *Plain-English Cause* -> *Safe Read-Only Check* -> *Verified Fix* -> *Environment Context*.
   - **Ask NEXUS (Phase 4 Grounded Assistant)**: A structured diagnostic assistant (not an open-ended chat toy) that automatically attaches the selected Device/Environment profile, searches saved Verified Recipes first, cites matching local records, and formats any new suggestion inside an `✨ AI Suggestion — Unverified` card with full safety badges.
4. **Tab 4: Systems (Devices, Environments, Backup & Audit Log)**
   - **Device & Environment Map**: Visual cards showing `Google Pixel 7 Pro` -> `Termux Host` -> `Debian proot` and `Windows 11 Laptop`, with recorded versions, ports (`8022`), and environment boundary cheat-sheets.
   - **Secret Guard & Data Control**: Export/Import full knowledge base as portable JSON/Markdown via Android's system file picker, plus the **Action & Verification History Log**.

---

## 6. Phase 0 Final Report Summary

### 6.1 Verified Facts
- The `zakshub/nexus` GitHub repository is on branch `main` with a clean working tree and 13 foundational documentation commits.
- The Antigravity development environment is a Debian 12 (`x86_64`) Linux VM with **OpenJDK 17.0.21**, **Git 2.39.5**, **OpenSSH 9.2p1**, **Python 3.11.2**, **Node.js v24.22.2**, and **19 GB of free disk space** in `/home/antigravity`.
- Android SDK (`sdkmanager`), Gradle (`gradle`), standalone `kotlinc`, `adb`, and Android Studio are **not** currently pre-installed in the Antigravity VM.
- Android's per-app UID sandbox and SELinux policy strictly forbid an ordinary Android APK from directly executing commands inside Termux's private sandbox or Debian `proot` without an explicit bridge (user copy-paste, Termux `RUN_COMMAND` permissioned Intent, or authenticated localhost/LAN SSH).

### 6.2 Constraints
- **No direct physical device link from Cloud VM:** The Antigravity VM cannot run `adb` against the owner's Pixel 7 Pro or directly probe the Windows 11 laptop.
- **Sandbox & environment separation:** Termux host (`Bionic libc`, `pkg`, port `8022`) and Debian `proot` (`glibc`, `apt`, simulated `/`) must always be modeled and displayed as separate environments.
- **12 GB RAM sharing on Pixel 7 Pro:** Running heavy local LLMs inside Android alongside Termux and Debian `proot` risks triggering Android's Phantom Process Killer or Out-of-Memory kills.

### 6.3 Recommended MVP Architecture
- **Platform:** Native Android app written in **Kotlin** using **Jetpack Compose (Material 3)**.
- **Persistence:** Offline-first **Room (SQLite + FTS4 full-text search)** for structured entities (`Device`, `Environment`, `ConnectionProfile`, `Recipe`, `Workflow`, `WorkflowStep`, `TroubleshootingCase`, `AuditLogEntry`) + **Jetpack DataStore** for preferences + **Android Keystore** for any future API/SSH credentials.
- **Pre-Seeded Starter Pack:** Ship Phase 2/3 with a curated, editable starter pack of recipes and troubleshooting guides tailored to the owner's exact stack (Windows 11 <-> Termux SSH on port `8022`, `termux-wake-lock`, Termux vs Debian `proot` commands, Git recovery) marked for owner verification.
- **AI Strategy (Phase 4):** Provider interface (`AiAssistantProvider`) supporting offline deterministic retrieval by default, plus opt-in remote Gemini API (with secret redaction and pre-send context preview) and future local LLM endpoint support.
- **Build & Delivery Strategy:** Set up standard Gradle Wrapper (`./gradlew`) + user-space Android SDK in the Antigravity VM (and/or GitHub Actions CI) so every milestone produces verified unit test results and a downloadable debug APK without forcing the owner to maintain a complex local Android Studio build toolchain on Windows 11.

### 6.4 Features in MVP vs. Postponed

| In Scope for Personal MVP (Phases 1–4) | Postponed to Later Phases (Phases 5–9) |
| --- | --- |
| Device & Environment profiles (`Windows 11`, `Pixel 7 Pro`, `Termux Host`, `Debian proot`) | Direct Termux `RUN_COMMAND` Intent execution bridge (Phase 5/6) |
| Execution Context Cards with Device, Environment, Prompt check, Plain-English breakdown, Safety & Verification badges | Localhost / LAN SSH automated inspection and allowlisted command runner (Phase 5/6) |
| Offline full-text search (FTS4) and instant filter by Device / Environment | Windows 11 companion service / desktop agent (Phase 7 — Optional) |
| Interactive step-by-step Guided Workflows with persistent progress state | On-device local LLM inference engine bundled inside the APK (Postponed due to APK size & RAM pressure) |
| Symptom -> Diagnosis -> Verified Fix Troubleshooter | Cloud user accounts, multi-user sync, subscriptions, or background telemetry (Out of scope) |
| Grounded AI Assistant interface with secret redaction & context preview (Phase 4) | Unrestricted shell terminal (Permanently excluded by security policy) |
| Client-side Secret Guard (blocks saving raw passwords/keys in notes) + Portable JSON/Markdown Export & Import | |

### 6.5 Unresolved Risks & Mitigations
1. **Risk: User runs a Debian command in Termux (or vice versa) despite documentation.**  
   *Mitigation:* Every recipe card displays a color-coded Environment Badge (`Termux` vs `Debian proot`), expected prompt preview, and a 1-line command to enter the right shell before the main command.
2. **Risk: Accidental storage or AI transmission of sensitive tokens/keys.**  
   *Mitigation:* Client-side `SecretGuardValidator` scans text before saving to Room or sending to any AI provider, blocking or redacting private keys, tokens, and passwords.
3. **Risk: Android build toolchain installation in the ephemeral Antigravity VM.**  
   *Mitigation:* In Phase 1 (after approval), install Android `cmdline-tools` cleanly in `/home/antigravity/android-sdk` AND add a GitHub Actions workflow (`.github/workflows/android-ci.yml`) so builds are reproducible both in the VM and on GitHub.

### 6.6 Revised Effort & Milestone Estimates
- **Phase 0 (Discovery & Validation):** 100% Complete pending owner sign-off (`M0.1`–`M0.4` completed in ~2 hours actual effort; `M0.5` awaiting owner approval).
- **Phase 1 (Android Foundation & UX Shell):** Estimated **3–4 hours** (Project scaffolding, Gradle wrapper, Material 3 theme, 4-tab navigation shell, Execution Context Card UI components, unit tests, and CI build verification).
- **Phase 2 (Technical Memory & Pre-Seeded Starter Pack):** Estimated **4–5 hours** (Room schema, FTS4 search index, Secret Guard validator, JSON/Markdown export/import, and pre-seeded Pixel 7 Pro / Termux / Debian / Windows 11 starter knowledge base).
- **Phase 3 (Search, Recipes & Resumable Guided Workflows):** Estimated **4–5 hours** (Interactive workflow runner with persistent step state, environment filters, verification status transitions, and troubleshooting case flows).
- **Phase 4 (Contextual Grounded Assistant):** Estimated **3–4 hours** (`AiAssistantProvider` abstraction, offline retrieval responder, optional Gemini provider with redaction + Context Preview dialog, and prompt-injection safety checks).

### 6.7 Owner Decisions Required (Phase 0 Approval Gate)
Before Phase 1 implementation begins, the owner must review and approve:
1. **Recommended MVP Architecture & Stack:** Native Android app in Kotlin + Jetpack Compose + Room (SQLite/FTS4) + DataStore + Android Keystore, focusing on searchable memory and guided handoff first (Phases 1–4) before any Termux execution bridge (Phases 5–6).
2. **UX Information Architecture:** The 4-tab layout (`Recall`, `Library`, `Troubleshoot`, `Systems`) and the 7-part **Execution Context Card** visual design.
3. **Phase 1 Build Authorization:** Permission to initialize the Android Gradle project in `zakshub/nexus`, install the Android command-line SDK tools into the Antigravity VM user space (`/home/antigravity/android-sdk`) to compile and run unit tests, and include a GitHub Actions build workflow so installable debug APKs are automatically built for the Pixel 7 Pro.

---

## 7. Exit criteria status

- [x] Development environment audited with verified evidence (`2026-11-12`).
- [x] Android, Termux, Debian `proot`, and Windows 11 integration boundaries documented.
- [x] MVP scope defined and separated from later execution phases.
- [x] Security boundaries and threat mitigations documented.
- [x] Architecture decisions recorded in `docs/DECISIONS.md`.
- [x] **Owner approves Phase 0 findings and authorizes Phase 1 (`2026-11-12`).**

