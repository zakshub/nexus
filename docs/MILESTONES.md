# Milestone Register

All dates and effort estimates are intentionally provisional until Phase 0 discovery.

| ID | Milestone | Phase | Deliverable | Dependency | Status |
| --- | --- | --- | --- | --- | --- |
| M0.1 | User workflow inventory | 0 | Ranked recurring workflows and pain points | None | Not started |
| M0.2 | Environment audit | 0 | Verified Windows, Android, Termux, Debian inventory | M0.1 | Not started |
| M0.3 | Integration feasibility | 0 | Documented device and sandbox boundaries | M0.2 | Not started |
| M0.4 | Architecture recommendation | 0 | Options, tradeoffs, risks, recommendation | M0.1 to M0.3 | Not started |
| M0.5 | Phase 0 approval | 0 | Owner approved scope and Phase 1 plan | M0.4 | Not started |
| M1.1 | Android project foundation | 1 | Buildable app shell | M0.5 | Blocked |
| M2.1 | Local memory schema | 2 | Database and repository layer | M1.1 | Blocked |
| M3.1 | Recipe search and guided flow | 3 | Search and step based guidance | M2.1 | Blocked |
| M4.1 | Contextual assistant | 4 | AI retrieval and response flow | M3.1 | Blocked |
| M5.1 | Read only device inspection | 5 | Permissioned inspection prototype | M4.1 and security review | Blocked |
| M6.1 | Approved operation runner | 6 | Allowlisted execution with audit | M5.1 and explicit approval | Blocked |
| M7.1 | Windows companion evaluation | 7 | Optional companion prototype | M6.1 | Optional |
| M8.1 | Personal APK release | 8 | Tested installable build and recovery docs | Core MVP acceptance | Blocked |
| M9.1 | Commercial feasibility review | 9 | Evidence based go or no go | Personal value proven | Optional |

## Completion policy

A milestone is complete only when its deliverable exists, acceptance criteria pass, evidence is recorded, and the owner accepts any user facing or security sensitive behavior.

Do not mark blocked work as in progress merely to improve progress statistics.
