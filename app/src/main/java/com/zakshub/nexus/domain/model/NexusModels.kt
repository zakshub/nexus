package com.zakshub.nexus.domain.model

/**
 * Physical device platform where an environment resides.
 */
enum class DevicePlatform(val displayName: String) {
    ANDROID("Android Phone"),
    WINDOWS("Windows 11 PC")
}

/**
 * Strictly separated execution environments.
 * Per ADR 0003, Termux Host and Debian proot must never be merged or treated as interchangeable.
 */
enum class ExecutionEnvironmentType(
    val badgeLabel: String,
    val shortLabel: String,
    val defaultPrompt: String,
    val packageManagerHint: String,
    val switchInstruction: String
) {
    TERMUX_HOST(
        badgeLabel = "Termux (Host Shell)",
        shortLabel = "Termux",
        defaultPrompt = "~ $",
        packageManagerHint = "Uses `pkg` (Android Bionic packages)",
        switchInstruction = "Open the Termux app directly, or connect from Windows via `ssh -p 8022 <phone-ip>`."
    ),
    DEBIAN_PROOT(
        badgeLabel = "Debian (proot inside Termux)",
        shortLabel = "Debian proot",
        defaultPrompt = "root@localhost:~#",
        packageManagerHint = "Uses `apt` (Debian GNU/Linux packages)",
        switchInstruction = "Inside Termux, run `proot-distro login debian` first and verify your prompt ends with `#`."
    ),
    WINDOWS_POWERSHELL(
        badgeLabel = "Windows 11 (PowerShell / SSH)",
        shortLabel = "Windows 11",
        defaultPrompt = "PS C:\\Users\\...>",
        packageManagerHint = "Uses `winget` / Windows OpenSSH client",
        switchInstruction = "Open Windows Terminal or PowerShell on your Windows 11 laptop."
    ),
    ANDROID_SYSTEM(
        badgeLabel = "Android OS Settings",
        shortLabel = "Android Settings",
        defaultPrompt = "Settings UI",
        packageManagerHint = "Pixel 7 Pro System Settings",
        switchInstruction = "Open the Android Settings app on your Google Pixel 7 Pro."
    )
}

/**
 * Safety and state-change impact level of a command or procedure step.
 */
enum class ImpactLevel(
    val badgeLabel: String,
    val plainEnglishSummary: String
) {
    READ_ONLY(
        badgeLabel = "Read-Only (Safe Check)",
        plainEnglishSummary = "Only inspects information. Changes nothing on your device."
    ),
    CONFIG_CHANGE(
        badgeLabel = "Changes Configuration",
        plainEnglishSummary = "Starts a service, installs a package, or updates a setting."
    ),
    HIGH_CAUTION(
        badgeLabel = "High Caution",
        plainEnglishSummary = "Stops active connections, removes files, or resets state. Review carefully."
    )
}

/**
 * Trust and verification state of a recipe or troubleshooting instruction.
 */
enum class VerificationStatus(
    val badgeLabel: String,
    val description: String
) {
    VERIFIED(
        badgeLabel = "Verified on My Setup",
        description = "Tested and confirmed working on your environment."
    ),
    UNVERIFIED_DRAFT(
        badgeLabel = "Starter / Unverified Draft",
        description = "Pre-loaded or saved note awaiting confirmation on your device."
    ),
    AI_SUGGESTED(
        badgeLabel = "AI Suggestion — Unverified",
        description = "Generated suggestion. Treat as untrusted until you verify it."
    )
}

/**
 * Plain-English explanation of a single command flag or parameter.
 */
data class CommandParameterBreakdown(
    val token: String,
    val meaningPlainEnglish: String
)

/**
 * Represents a physical device owned by the user.
 */
data class DeviceProfile(
    val id: String,
    val name: String,
    val platform: DevicePlatform,
    val hardwareSummary: String,
    val roleDescription: String,
    val isVerified: Boolean
)

/**
 * Represents a distinct execution environment residing on a device.
 */
data class EnvironmentProfile(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val type: ExecutionEnvironmentType,
    val shellSummary: String,
    val filesystemBoundaryNote: String,
    val commonPitfallWarning: String
)

/**
 * The 7-part Execution Context Card model representing a command recipe.
 */
data class CommandRecipe(
    val id: String,
    val title: String,
    val targetDeviceName: String,
    val targetPlatform: DevicePlatform,
    val environmentType: ExecutionEnvironmentType,
    val purposePlainEnglish: String,
    val commandSyntax: String,
    val parameters: List<CommandParameterBreakdown>,
    val expectedOutputPlainEnglish: String,
    val impactLevel: ImpactLevel,
    val verificationStatus: VerificationStatus,
    val rollbackOrUndoHint: String? = null,
    val isPinnedDaily: Boolean = false
)

/**
 * A single step inside a resumable Guided Workflow.
 */
data class GuidedWorkflowStep(
    val stepNumber: Int,
    val title: String,
    val targetDeviceName: String,
    val environmentType: ExecutionEnvironmentType,
    val explanationPlainEnglish: String,
    val commandToRun: String,
    val expectedResult: String,
    val impactLevel: ImpactLevel,
    val ifItFailsHint: String,
    val isCompleted: Boolean = false
)

/**
 * A multi-step guided procedure that remembers which step the user is on.
 */
data class GuidedWorkflow(
    val id: String,
    val title: String,
    val summaryPlainEnglish: String,
    val steps: List<GuidedWorkflowStep>
) {
    val completedStepsCount: Int
        get() = steps.count { it.isCompleted }

    val nextIncompleteStepNumber: Int
        get() = steps.firstOrNull { !it.isCompleted }?.stepNumber ?: steps.size
}

/**
 * Structured Symptom -> Plain-English Cause -> Safe Check -> Verified Fix record.
 */
data class TroubleshootingCase(
    val id: String,
    val symptomTitle: String,
    val exactErrorSnippet: String,
    val targetDeviceName: String,
    val environmentType: ExecutionEnvironmentType,
    val plainEnglishWhyItHappens: String,
    val safeReadOnlyCheckCommand: String,
    val verifiedFixCommand: String,
    val verificationStatus: VerificationStatus
)

/**
 * A single step in an executable, audited mobile terminal workflow plan.
 */
data class AgentActionStep(
    val stepNumber: Int,
    val title: String,
    val targetEnvironment: ExecutionEnvironmentType,
    val expectedPrompt: String,
    val commandSyntax: String,
    val purposePlainEnglish: String,
    val expectedResult: String,
    val impactLevel: ImpactLevel = ImpactLevel.READ_ONLY,
    val isCompleted: Boolean = false
)

/**
 * An executable multi-step Agent plan generated from natural language intent.
 */
data class AgentWorkflowPlan(
    val goalSummary: String,
    val targetDeviceName: String = "Google Pixel 7 Pro",
    val steps: List<AgentActionStep>,
    val safetyNotice: String? = null,
    val provenance: VerificationStatus = VerificationStatus.AI_SUGGESTED
)
