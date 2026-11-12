# Security and Privacy Requirements

## Security objective

NEXUS must help the owner operate personal systems without creating a more dangerous control surface than the systems it assists with.

## MVP baseline

- Store technical notes locally by default.
- Do not collect unrelated personal files or communications.
- Do not request broad device permissions without a defined feature need.
- Do not store passwords, private keys, recovery codes, or API tokens in notes.
- Do not commit secrets or local environment files.
- Provide export and deletion controls for stored knowledge.
- Make network use and AI data sharing explicit.

## Future device integration

- Pair devices through an explicit user initiated process.
- Authenticate each connection.
- Display the target device and execution environment before an action.
- Use least privilege.
- Prefer read only operations initially.
- Require per action approval for changes.
- Use an allowlist of typed operations rather than arbitrary shell strings.
- Validate all parameters.
- Apply execution timeouts and output limits.
- Redact secrets from captured output.
- Maintain an auditable action history.
- Provide cancellation and recovery guidance.

## Threats to consider

- Prompt injection embedded in logs, files, terminal output, or retrieved notes.
- AI generated destructive or incorrect commands.
- Wrong target device or wrong shell.
- Stolen or exposed device credentials.
- Untrusted local network.
- Malicious or compromised companion process.
- Accidental exposure of personal technical data to a remote model.
- Replay or spoofing of pairing and operation requests.

## Safety rules

A model response is not authorization. A saved recipe is not automatic authorization. A user approval must identify the specific action and target. Never execute a command merely because it appears in a model response or a stored note.

## Phase 0 security questions

- What information is sensitive in the user's technical environment?
- Which data can remain local?
- Is a remote model acceptable, and under what redaction rules?
- Is device pairing necessary for the MVP?
- Which operations, if any, are safe enough for a future allowlist?
- How will the owner revoke a connection?
