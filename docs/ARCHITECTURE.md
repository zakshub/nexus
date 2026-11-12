# Architecture Proposal

**Status:** Initial proposal. All choices require Phase 0 validation.

## Proposed high level structure

1. Android application: primary user interface and local experience.
2. Local knowledge store: devices, environments, recipes, troubleshooting cases, workflows, and action history.
3. Context and retrieval layer: selects relevant saved information for a user question.
4. AI provider interface: allows a remote or local model to be changed without rewriting product logic.
5. Optional device bridge: a separately authenticated component for future inspection or approved operations.
6. Windows companion: a later option if Android alone cannot safely or reliably reach Windows workflows.

## Candidate technology

| Area | Candidate | Reason to validate |
| --- | --- | --- |
| Android UI | Kotlin and Jetpack Compose | Native Android interface and modern UI toolkit |
| Local database | Room over SQLite | Structured local persistence |
| Preferences | DataStore | Small application settings |
| Background work | WorkManager | Deferred, system managed work when required |
| AI integration | Provider interface | Avoid locking product logic to one model |
| Local automation | Explicit scripts or allowlisted operations | Controlled integration with host environments |
| Device communication | Authenticated SSH or companion bridge | Must respect OS boundaries and user approval |

These are candidates, not final decisions.

## Data model candidates

- Device: human readable name, platform, model, ownership label.
- Environment: device reference, operating system, shell, runtime, filesystem boundary.
- Connection: device reference, protocol, endpoint metadata, trust state. Never store raw private keys in ordinary records.
- Recipe: title, purpose, target environment, commands, parameters, expected output, risk level, verification status.
- Troubleshooting case: symptom, error text, environment, diagnosis, resolution, evidence, status.
- Workflow: ordered steps, prerequisites, completion state, rollback guidance.
- Action record: requested operation, target, approval, result, timestamp, redacted output.

## Critical platform boundary

An ordinary Android application runs inside its own sandbox. It must not assume it can invoke arbitrary commands inside Termux or Debian proot. Termux and Debian proot are separate execution contexts. Any integration needs an explicit, documented bridge, such as a user initiated copy and paste flow, a Termux supported integration mechanism, or a separately authenticated service. Phase 0 must verify feasibility and security before selecting one.

## Security architecture requirements

- No unrestricted shell interface in the MVP.
- No model generated command may execute directly.
- Every future operation must be represented by a typed, allowlisted action.
- Validate parameters and target environment.
- Require explicit approval immediately before state changing actions.
- Apply timeouts and output size limits.
- Redact secrets from logs and AI context.
- Record operation and outcome.
- Provide cancellation and rollback guidance where feasible.
- Use platform secure storage for credentials if credentials become necessary.

## AI design requirements

- Treat model responses as untrusted.
- Retrieve only relevant user approved context.
- Mark uncertain recommendations.
- Distinguish saved verified recipes from newly generated suggestions.
- Never imply a command was executed unless execution evidence confirms it.
- Keep provider credentials outside source control.

## Architecture decisions still open

- Kotlin native app versus cross platform framework.
- Local only versus optional remote model.
- Whether a companion service is required.
- How to bridge Android app and Termux safely.
- Whether Windows needs a companion agent.
- Backup and encryption model.
