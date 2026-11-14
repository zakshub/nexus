package com.zakshub.nexus.ai

import com.zakshub.nexus.data.StarterKnowledgeCatalog
import com.zakshub.nexus.domain.model.AgentActionStep
import com.zakshub.nexus.domain.model.AgentWorkflowPlan
import com.zakshub.nexus.domain.model.CommandRecipe
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.ImpactLevel
import com.zakshub.nexus.domain.model.VerificationStatus
import com.zakshub.nexus.security.SecretGuardValidator

/**
 * Mobile-First Terminal Action Agent (Phase 6 / ADR 0003, ADR 0006, ADR 0007).
 *
 * Converts natural language goals (in Urdu or English) into an audited,
 * step-by-step terminal execution plan strictly separating Termux Host and Debian PRoot.
 */
object MobileTerminalAgent {

    /**
     * Synthesizes an executable agent plan from user intent.
     * Guaranteed deterministic, secret-guarded, and offline-capable.
     */
    fun createPlan(
        userIntent: String,
        targetEnvironmentScope: ExecutionEnvironmentType? = null,
        availableRecipes: List<CommandRecipe> = StarterKnowledgeCatalog.recipes
    ): AgentWorkflowPlan {
        val sanitized = ContextRedactor.sanitizeAndRedact(userIntent)
        val q = sanitized.lowercase().trim()

        // High-risk command check on raw input
        val highRiskWarning = ContextRedactor.detectHighRiskCommand(q)
        if (highRiskWarning != null) {
            return AgentWorkflowPlan(
                goalSummary = "Blocked: Potentially Destructive Command Detected",
                steps = emptyList(),
                safetyNotice = highRiskWarning,
                provenance = VerificationStatus.UNVERIFIED_DRAFT
            )
        }

        // Pattern 1: Storage & Downloads Exploration (Termux Host)
        if (q.contains("download") || q.contains("ڈاؤن لوڈ") || q.contains("storage") || q.contains("اسٹوریج") || q.contains("فولڈر")) {
            val steps = listOf(
                AgentActionStep(
                    stepNumber = 1,
                    title = "Ensure Storage Permission",
                    targetEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
                    expectedPrompt = "~ $",
                    commandSyntax = "termux-setup-storage",
                    purposePlainEnglish = "Grants Termux permission to access phone storage and creates `~/storage` links.",
                    expectedResult = "Android permission popup (Allow).",
                    impactLevel = ImpactLevel.CONFIG_CHANGE
                ),
                AgentActionStep(
                    stepNumber = 2,
                    title = "Explore Downloads Directory",
                    targetEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
                    expectedPrompt = "~ $",
                    commandSyntax = "cd ~/storage/downloads && pwd && ls -lh",
                    purposePlainEnglish = "Switches into the phone's Downloads directory and lists files with human-readable sizes.",
                    expectedResult = "List of downloaded files.",
                    impactLevel = ImpactLevel.READ_ONLY
                )
            )
            return AgentWorkflowPlan(
                goalSummary = "Explore Phone Storage & Downloads in Termux",
                steps = steps,
                safetyNotice = "Safe read-only folder exploration inside phone storage.",
                provenance = VerificationStatus.VERIFIED
            )
        }

        // Pattern 2: Switch into Debian & Explore Linux Workspace (Two-Stage Transition)
        if (q.contains("debian") || q.contains("ڈیبین") || q.contains("proot") || q.contains("پروٹ") || q.contains("لینکس") || q.contains("linux")) {
            val steps = listOf(
                AgentActionStep(
                    stepNumber = 1,
                    title = "Launch Debian PRoot Container",
                    targetEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
                    expectedPrompt = "~ $",
                    commandSyntax = "proot-distro login debian",
                    purposePlainEnglish = "Switches from native Termux host shell into the full Debian GNU/Linux environment.",
                    expectedResult = "Prompt changes to `root@localhost:~#`.",
                    impactLevel = ImpactLevel.READ_ONLY
                ),
                AgentActionStep(
                    stepNumber = 2,
                    title = "Navigate to Debian Root Workspace",
                    targetEnvironment = ExecutionEnvironmentType.DEBIAN_PROOT,
                    expectedPrompt = "root@localhost:~#",
                    commandSyntax = "cd /root && pwd && ls -la",
                    purposePlainEnglish = "Enters root user home directory and inspects active project repositories and dotfiles.",
                    expectedResult = "Shows `/root` and directory listings.",
                    impactLevel = ImpactLevel.READ_ONLY
                ),
                AgentActionStep(
                    stepNumber = 3,
                    title = "Return to Termux Host When Done",
                    targetEnvironment = ExecutionEnvironmentType.DEBIAN_PROOT,
                    expectedPrompt = "root@localhost:~#",
                    commandSyntax = "exit",
                    purposePlainEnglish = "Safely exits Debian container and returns to the outer Termux host shell.",
                    expectedResult = "Prompt changes back to '~ \$'.",
                    impactLevel = ImpactLevel.READ_ONLY
                )
            )
            return AgentWorkflowPlan(
                goalSummary = "Two-Stage Transition: Enter Debian, Work in Linux, Return to Termux",
                steps = steps,
                safetyNotice = "Notice the prompt changes from '\$' (Termux) to '#' (Debian) and back.",
                provenance = VerificationStatus.VERIFIED
            )
        }

        // Pattern 3: Git Status & Project Inspection inside Debian
        if (q.contains("git") || q.contains("گٹ") || q.contains("pull") || q.contains("status") || q.contains("پروجیکٹ")) {
            val steps = listOf(
                AgentActionStep(
                    stepNumber = 1,
                    title = "Enter Debian PRoot",
                    targetEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
                    expectedPrompt = "~ $",
                    commandSyntax = "proot-distro login debian",
                    purposePlainEnglish = "Switches into Debian Linux where your git repositories reside.",
                    expectedResult = "Prompt shows `root@localhost:~#`.",
                    impactLevel = ImpactLevel.READ_ONLY
                ),
                AgentActionStep(
                    stepNumber = 2,
                    title = "Check Project Git Status",
                    targetEnvironment = ExecutionEnvironmentType.DEBIAN_PROOT,
                    expectedPrompt = "root@localhost:~#",
                    commandSyntax = "cd /root && git status 2>/dev/null || ls -la",
                    purposePlainEnglish = "Inspects git repository status or lists directory contents if not currently in a git repo.",
                    expectedResult = "Shows branch name, modified files, or directory listing.",
                    impactLevel = ImpactLevel.READ_ONLY
                )
            )
            return AgentWorkflowPlan(
                goalSummary = "Check Git Status and Project Files in Debian PRoot",
                steps = steps,
                safetyNotice = "Read-only inspection. No remote changes or destructive actions.",
                provenance = VerificationStatus.VERIFIED
            )
        }

        // Pattern 4: Package Updates
        if (q.contains("update") || q.contains("اپڈیٹ") || q.contains("upgrade") || q.contains("install")) {
            val isDebian = targetEnvironmentScope == ExecutionEnvironmentType.DEBIAN_PROOT || q.contains("debian") || q.contains("apt")
            val steps = if (isDebian) {
                listOf(
                    AgentActionStep(
                        stepNumber = 1,
                        title = "Update Debian Linux Packages (apt)",
                        targetEnvironment = ExecutionEnvironmentType.DEBIAN_PROOT,
                        expectedPrompt = "root@localhost:~#",
                        commandSyntax = "apt update && apt upgrade -y",
                        purposePlainEnglish = "Fetches latest Debian ARM64 security updates and packages inside PRoot.",
                        expectedResult = "Package lists updated successfully.",
                        impactLevel = ImpactLevel.CONFIG_CHANGE
                    )
                )
            } else {
                listOf(
                    AgentActionStep(
                        stepNumber = 1,
                        title = "Update Termux Native Packages (pkg)",
                        targetEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
                        expectedPrompt = "~ $",
                        commandSyntax = "pkg update && pkg upgrade -y",
                        purposePlainEnglish = "Updates Android Termux packages and package mirrors.",
                        expectedResult = "Termux packages upgraded.",
                        impactLevel = ImpactLevel.CONFIG_CHANGE
                    )
                )
            }
            return AgentWorkflowPlan(
                goalSummary = if (isDebian) "Update Debian Linux Packages inside PRoot" else "Update Termux Native Packages",
                steps = steps,
                safetyNotice = "Installs software updates. Ensure your phone is connected to Wi-Fi.",
                provenance = VerificationStatus.VERIFIED
            )
        }

        // Pattern 5: Memory / Battery / IP Inspection
        if (q.contains("ram") || q.contains("memory") || q.contains("میموری") || q.contains("ip") || q.contains("wifi") || q.contains("نیٹ ورک")) {
            val steps = listOf(
                AgentActionStep(
                    stepNumber = 1,
                    title = "Check Phone Active RAM and Resource Limits",
                    targetEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
                    expectedPrompt = "~ $",
                    commandSyntax = "free -h",
                    purposePlainEnglish = "Displays active, used, and free RAM on your Pixel 7 Pro in gigabytes.",
                    expectedResult = "Memory breakdown table (~11Gi total).",
                    impactLevel = ImpactLevel.READ_ONLY
                ),
                AgentActionStep(
                    stepNumber = 2,
                    title = "Check Phone Wi-Fi IPv4 Address",
                    targetEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
                    expectedPrompt = "~ $",
                    commandSyntax = "ip -4 addr show wlan0 | grep inet",
                    purposePlainEnglish = "Prints the phone's active local network address (e.g., 192.168.x.x).",
                    expectedResult = "inet 192.168.x.x address line.",
                    impactLevel = ImpactLevel.READ_ONLY
                )
            )
            return AgentWorkflowPlan(
                goalSummary = "Inspect Mobile Hardware Resources (RAM & IP)",
                steps = steps,
                safetyNotice = "100% safe read-only hardware inspection.",
                provenance = VerificationStatus.VERIFIED
            )
        }

        // Pattern 6: General Fallback Plan
        val env = targetEnvironmentScope ?: ExecutionEnvironmentType.TERMUX_HOST
        val steps = listOf(
            AgentActionStep(
                stepNumber = 1,
                title = "Verify Active Environment Location",
                targetEnvironment = env,
                expectedPrompt = env.defaultPrompt,
                commandSyntax = if (env == ExecutionEnvironmentType.DEBIAN_PROOT) "pwd && cat /etc/os-release" else "pwd && whoami",
                purposePlainEnglish = "Verifies your current directory and environment identity before executing novel commands.",
                expectedResult = "Shows active working path and user identity.",
                impactLevel = ImpactLevel.READ_ONLY
            )
        )
        return AgentWorkflowPlan(
            goalSummary = "Guided Exploration: ${sanitized.take(60)}",
            steps = steps,
            safetyNotice = "Step synthesized for $sanitized. Review the command before running in Termux.",
            provenance = VerificationStatus.AI_SUGGESTED
        )
    }
}
