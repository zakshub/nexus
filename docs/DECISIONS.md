# Architecture Decision Log

Record decisions with date, context, options, rationale, consequences, and status.

## ADR 0001: Discovery before implementation

**Status:** Accepted  
**Decision:** Complete Phase 0 discovery and receive owner approval before implementing the Android application.  
**Reason:** Platform boundaries and the actual MVP are not yet validated.  
**Consequence:** App implementation remains blocked until the Phase 0 gate is passed.

## ADR 0002: Memory before autonomous execution

**Status:** Accepted  
**Decision:** Prioritize searchable technical memory and guided workflows before command execution.  
**Reason:** Memory can deliver value with substantially less security and integration risk.  
**Consequence:** Unrestricted shell execution is excluded from the MVP.

## ADR 0003: Treat Termux and Debian proot as separate environments

**Status:** Accepted  
**Decision:** Store and display Termux host and Debian proot as distinct execution targets.  
**Reason:** They have different filesystem, package, process, and environment boundaries.  
**Consequence:** Every command recipe must declare its target environment.

## Pending decisions

- Native Kotlin and Jetpack Compose or another framework.
- Local database and backup strategy.
- Local model versus remote model support.
- Termux integration mechanism.
- Whether a Windows companion is required.
- Device pairing and authentication model.
