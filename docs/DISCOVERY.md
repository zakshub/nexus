# Phase 0 Discovery

**Status:** Not started  
**Purpose:** Validate the problem, environment, integration boundaries, architecture, and MVP before implementation.

## 1. User workflow discovery

Collect and rank real recurring workflows. Start with:
- Windows to Termux SSH connection and troubleshooting.
- Distinguishing Termux from Debian proot.
- Recalling Linux commands and where to run them.
- Git and GitHub setup and recovery.
- Antigravity setup and project execution.
- Repeating device configuration and development environment checks.

For each workflow record frequency, time lost, failure impact, current workaround, and what NEXUS should remember.

## 2. Environment audit

Verify, do not assume:
- Windows version and shell.
- Git and GitHub CLI availability and versions.
- Android version and device details.
- Termux version and installed packages.
- Debian proot distribution and version.
- SSH server state, listening port, network route, and firewall constraints.
- Android SDK, JDK, Gradle, ADB, and Android Studio availability.

Do not collect credentials, private keys, or unrelated personal data.

## 3. Integration feasibility

Document:
- Android app sandbox restrictions.
- Termux integration options and required user actions.
- Debian proot filesystem and process boundaries.
- Windows SSH client to Termux SSH server connection requirements.
- Local network and hotspot behavior.
- What happens when the phone sleeps or the network changes.
- Whether a companion service is justified.

## 4. Architecture comparison

Compare at least:
- Native Android app.
- Cross platform app.
- Local first app with manual command handoff.
- Android app plus authenticated Termux bridge.
- Android app plus Windows companion.

Evaluate user effort, reliability, security, maintenance, offline support, implementation cost, and future flexibility.

## 5. Phase 0 report template

### Verified facts
List facts and evidence.

### User workflow ranking
List the highest value repeated workflows.

### Constraints
List platform and environment limitations.

### Options
Compare viable architecture choices.

### Recommendation
State the recommended MVP architecture and why.

### Risks
List unresolved risks and mitigation.

### Estimate
Revise effort and milestone estimates.

### Owner decisions
List decisions requiring explicit approval.

## 6. Exit criteria

Phase 0 is complete when the environment is audited, major integration assumptions are tested or clearly marked unresolved, MVP scope is defined, security boundaries are documented, and the owner approves Phase 1.
