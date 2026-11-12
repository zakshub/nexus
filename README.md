# NEXUS

**Your Systems. Your Memory. Your Intelligence.**

NEXUS is a personal, security conscious technical assistant intended to help its owner understand, remember, troubleshoot, and operate their computing environments. Its first target is a personal Android app connected to a Windows laptop and an Android phone running Termux and Debian through proot.

## Product status

**Stage:** Phase 5 Complete (Live Read-Only Environment Inspector & Port Probing Active)  
**Release:** `0.10.0` (`release/nexus-v0.10.0.apk` 56MB verified, 26/26 unit tests passing)  
**Implementation:** Personal MVP + Fast-Track Daily Routines + Phase 5 Live Environment Inspector (**100% complete**)  
**Direct APK Download:** [Download NEXUS APK v0.10.0](https://github.com/zakshub/nexus/raw/main/release/nexus-v0.10.0.apk)  
**User guide:** See [5-Minute Owner Playbook](docs/OWNER_PLAYBOOK.md) for installation and daily usage.

## The problem

Technical knowledge about devices, commands, network addresses, SSH setup, Linux environments, errors, and fixes is scattered across terminal history, notes, chat conversations, and memory. Repeating setup and troubleshooting wastes time and makes it easy to run the wrong command in the wrong environment.

NEXUS aims to turn that fragmented knowledge into a searchable, structured, reusable personal technical memory with guided assistance.

## Product principles

1. Remember verified solutions, not unverified guesses.
2. Identify the target device and execution environment before recommending an operation.
3. Explain commands in plain language before asking the user to run them.
4. Start with read only inspection and guided workflows.
5. Require explicit approval for any operation that changes a system.
6. Never store passwords, private keys, or API secrets as ordinary notes.
7. Preserve a clear audit trail of actions and outcomes.
8. Prefer useful personal software over premature platform complexity.

## Initial environment

- Windows 11 laptop
- Google Pixel 7 Pro running Android
- Termux on Android
- Debian proot inside Termux
- OpenSSH server in Termux
- Windows SSH client
- Git and GitHub
- Antigravity and Gemini for assisted development
- Potential future local language model

These are user reported facts and must be rechecked during discovery. Do not assume Android SDK, Java, Gradle, ADB, or build tools are installed.

## Proposed documentation

- [5-minute owner playbook](docs/OWNER_PLAYBOOK.md)
- [Product brief](docs/PRODUCT_BRIEF.md)
- [Vision and mission](docs/VISION_AND_MISSION.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Milestones](docs/MILESTONES.md)
- [Task register](docs/TASKS.md)
- [Security model](docs/SECURITY.md)
- [Discovery plan](docs/DISCOVERY.md)
- [Environment inventory template](docs/ENVIRONMENT_INVENTORY_TEMPLATE.md)
- [Architecture decisions](docs/DECISIONS.md)
- [Antigravity operating guide](ANTIGRAVITY.md)
- [Agent instructions](AGENTS.md)

## First milestone

Complete Phase 0 only. Inspect the real environment, validate constraints, compare architecture options, document risks, and present a recommendation. Do not start app implementation until the owner approves the Phase 0 findings.

## Scope boundaries

NEXUS is not intended to be an unrestricted remote shell, a generic chatbot wrapper, a replacement for professional system administration, or an autonomous agent with blanket access to personal devices. Remote execution is a later capability and must be permissioned, constrained, observable, and reversible where possible.

## Progress tracking

Update the roadmap, milestone register, task register, and decision log whenever project status changes. Never mark work complete without evidence such as a passing test, verified behavior, or owner acceptance.
