# Security and Privacy Requirements

**Status:** Phase 0 Validated Security Model (`2026-11-12`)

## 1. Security objective

NEXUS must help the owner operate personal systems without creating a more dangerous control surface than the systems it assists with.

---

## 2. Non-negotiable security boundaries (MVP and beyond)

1. **No unrestricted shell execution:** NEXUS will never provide an open-ended remote shell or execute arbitrary command strings.
2. **No automatic destructive commands:** Every command is classified by impact (`READ_ONLY`, `CONFIG_CHANGE`, `HIGH_CAUTION`), and state-changing actions require explicit user action and rollback notes.
3. **No passwords or private keys in ordinary notes:** A deterministic client-side `SecretGuardValidator` inspects all text before saving to Room SQLite, exporting to files, or sending to an AI provider, blocking or redacting:
   - PEM/OpenSSH private keys (`-----BEGIN ... PRIVATE KEY-----`)
   - GitHub Personal Access Tokens (`ghp_...`, `github_pat_...`, `gho_...`)
   - Google / Gemini API keys (`AIza...`) and OpenAI/Anthropic style keys (`sk-...`)
   - Bearer tokens and inline password parameters (`password=...`, `passwd=...`, `secret=...`)
4. **No silent device access:** Phases 1–3 require **zero network permissions (`INTERNET` omitted)** and zero background services.
5. **Explicit approval before state-changing operations:** In future execution phases (Phase 5–6), every action requires immediate, per-action user confirmation showing exact target device, target environment, command, parameters, and risk level.
6. **Strict command & parameter validation:** Future automated operations use a compiled allowlist of typed operation templates with strict parameter regex validation (rejecting shell metacharacters `;`, `&&`, `||`, `` ` ``, `$(...)`, `|`, `>`).
7. **Immutable local audit trail:** Record all recipe verifications, exports, blocked secret alerts, and future approved operations with timestamps and redacted outputs in `AuditLogEntity`.
8. **Treat AI-generated commands as untrusted:**
   - AI output is never passed to a shell or clipboard without explicit user action.
   - AI suggestions are visually isolated with an `✨ AI Suggested — Unverified` badge and dashed warning border.
   - Terminal logs or error outputs pasted by the user are wrapped in strict data delimiters and sanitized before AI retrieval to defend against indirect prompt injection.

---

## 3. Future device integration controls (Phases 5–7)

- Pair devices through an explicit user-initiated process.
- Authenticate each connection using hardware-backed keys stored in **Android Keystore** (Pixel 7 Pro Titan M2).
- Display the target device and execution environment (`Termux Host` vs `Debian proot` vs `Windows 11`) before any action.
- Use least privilege and start with read-only inspection (Phase 5) before any state-changing operation (Phase 6).
- Apply strict execution timeouts (e.g., 10 seconds for status checks) and output size limits (e.g., 64 KB cap) to prevent resource exhaustion.
- Provide one-tap connection revocation and key deletion in the `Systems` tab.

---

## 4. Threat model and mitigations

| Threat | Severity | Concrete Mitigation in NEXUS |
| --- | --- | --- |
| **1. Running a valid command in the wrong environment** (e.g., Debian command inside Termux host or vice versa) | High | Mandatory **Target Device Pill** + **Environment Badge** + **Expected Prompt Strip** (`~ $` vs `root@localhost:~#`) on every card. |
| **2. AI-generated destructive or hallucinated flags** | High | Offline-first retrieval prioritizes saved `✅ Verified` recipes; AI suggestions are badged `✨ Unverified`, checked against a high-risk command detector (`rm -rf`, `dd`, `mkfs`, `chmod -R 777`), and never auto-executed. |
| **3. Indirect prompt injection inside terminal error logs** | Medium–High | Pasted terminal logs are secret-redacted, length-capped, and enclosed in untrusted-data structural tags; AI responses cannot trigger tool execution. |
| **4. Accidental storage or export of SSH keys, API tokens, or passwords** | High | `SecretGuardValidator` blocks saving raw secrets into Room notes/recipes and strips any secret patterns before export or AI context packaging. |
| **5. Unauthorized local network access to Termux `sshd`** | Medium–High | NEXUS recipes guide the user to use SSH key authentication (disabling password auth in `sshd_config`), bind/firewall appropriately, and stop `sshd` (`pkill sshd`) on untrusted public Wi-Fi. |
| **6. Accidental leak of personal environment data to a remote AI** | Medium | Remote AI is opt-in (Phase 4) and displays a mandatory **Context Preview Dialog** showing the exact redacted text that will be sent before any network request is made. |

---

## 5. Safety rules

A model response is not authorization. A saved recipe is not automatic authorization. A user approval must identify the specific action and target. Never execute a command merely because it appears in a model response or a stored note.

---

## 6. Phase 0 security questions — Resolved

1. **What information is sensitive in the user's technical environment?**  
   SSH private keys, GitHub tokens, AI API keys, account passwords, and public IP addresses. Non-sensitive metadata (device model, OS release, Termux port `8022`, shell paths, package names) is safe to store locally.
2. **Which data can remain local?**  
   100% of stored knowledge (devices, environments, recipes, workflows, troubleshooting cases, audit logs) remains local on the Pixel 7 Pro in Room SQLite.
3. **Is a remote model acceptable, and under what redaction rules?**  
   Yes, as an opt-in feature in Phase 4 (`RemoteGeminiProvider`), provided that: (a) Phases 1–3 work 100% offline without it, (b) `SecretGuardValidator` redacts all secret patterns, IPs, and paths automatically, and (c) the user sees a Context Preview before sending.
4. **Is device pairing necessary for the MVP?**  
   **No.** Phases 1–4 use zero-setup Guided Copy-and-Paste Handoff, eliminating pairing and network attack surfaces from the MVP.
5. **Which operations, if any, are safe enough for a future allowlist?**  
   In Phase 5, strictly read-only status checks with zero arguments: `uname -a`, `df -h`, `free -h`, `ip -4 addr show wlan0`, `pgrep -a sshd`, and checking `/etc/os-release`.
6. **How will the owner revoke a connection?**  
   In Phase 4+, a single "Clear Credentials & Revoke Access" control in the `Systems` screen deletes Android Keystore keys; on Termux, a pinned recipe guides removing the key from `~/.ssh/authorized_keys` or disabling `allow-external-apps`.

