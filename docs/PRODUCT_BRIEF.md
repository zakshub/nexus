# Product Brief

## Product

NEXUS is a personal technical memory and guided operations assistant.

**Working tagline:** Your Systems. Your Memory. Your Intelligence.

## User and context

The initial user works across a Windows 11 laptop and an Android phone. The phone uses Termux and Debian through proot. The user connects from Windows to Termux over SSH and works with GitHub and Antigravity. The user is comfortable with design tools and guided technical workflows but does not want to memorize Linux administration commands.

This is a single owner product initially. Do not assume a public multi user market.

## Problem statement

Device configuration, command history, troubleshooting steps, network details, and successful fixes are fragmented across terminal sessions, chat histories, notes, and memory. When a problem returns, the user must rediscover the correct environment, command, and sequence. Incorrect assumptions about the target environment can cause failed commands or unintended changes.

## Product outcome

A user can ask a technical question and receive a context aware answer grounded in their saved environment, verified command recipes, and previous troubleshooting outcomes. The system identifies the target device and execution context, explains the next step, and guides the user safely.

## Core jobs to be done

1. Remember my devices, environments, and verified configurations.
2. Find a solution I used before.
3. Explain what a command does and where it must run.
4. Help diagnose an error using relevant environment context.
5. Guide me through a multi step setup without losing progress.
6. Eventually perform approved, constrained operations and verify their results.

## MVP hypothesis

A searchable local knowledge base plus structured device profiles, command recipes, and guided troubleshooting will provide meaningful value before any autonomous execution is built.

## MVP capabilities under consideration

- Device and environment profiles
- Searchable technical notes
- Verified command recipes with target environment labels
- Troubleshooting records with symptoms, causes, and confirmed fixes
- Guided step sequences and progress state
- AI assistance grounded in saved context
- Import and export of user controlled data

## Explicitly out of scope for the first MVP

- Unrestricted remote terminal
- Silent command execution
- Automatic access to private messages or personal files
- Always on background monitoring
- Cloud account system
- Multi user collaboration
- Commercial subscription and billing
- Broad device management across arbitrary operating systems

## Success measures

- User can retrieve a saved solution quickly.
- Every command recipe clearly identifies its target environment.
- User can distinguish verified instructions from AI suggestions.
- A guided workflow can resume after the app closes.
- No secret is stored in ordinary notes or logs.
- Any state changing action requires explicit approval.
- The user can export their knowledge without vendor lock in.

## Key risks

- Android sandbox limitations may prevent direct integration with Termux.
- A companion service may add setup and maintenance burden.
- AI may generate incorrect or unsafe commands.
- Local model performance and memory requirements may constrain the experience.
- The product may become a generic chatbot unless its structured memory is genuinely useful.

## Validation questions

- Which repeated workflows cause the most friction?
- Which information must be remembered persistently?
- Is copy and paste guidance sufficient for the first release?
- Is direct execution worth the added security and setup cost?
- Does the owner want the first version to work fully offline?
