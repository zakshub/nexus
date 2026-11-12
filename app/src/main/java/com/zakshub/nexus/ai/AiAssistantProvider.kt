package com.zakshub.nexus.ai

import com.zakshub.nexus.domain.model.CommandRecipe
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.ImpactLevel
import com.zakshub.nexus.domain.model.VerificationStatus
import com.zakshub.nexus.security.SecretGuardValidator
import kotlinx.serialization.Serializable

@Serializable
data class AssistantCitation(
    val recordId: String,
    val title: String,
    val environmentType: String,
    val relevanceSnippet: String
)

@Serializable
data class AssistantDiagnosticResponse(
    val plainEnglishDiagnosis: String,
    val suggestedCommand: String?,
    val targetEnvironment: ExecutionEnvironmentType,
    val impactLevel: ImpactLevel,
    val verificationStatus: VerificationStatus,
    val citations: List<AssistantCitation>,
    val highRiskWarning: String? = null,
    val isGeneratedByAi: Boolean = false
)

data class DiagnosticQueryContext(
    val userQuestion: String,
    val errorSnippet: String? = null,
    val selectedEnvironment: ExecutionEnvironmentType? = null,
    val availableVerifiedRecipes: List<CommandRecipe> = emptyList(),
    val knownCases: List<com.zakshub.nexus.domain.model.TroubleshootingCase> = emptyList()
)

interface AiAssistantProvider {
    val providerName: String
    val isOfflineOnly: Boolean

    suspend fun diagnose(context: DiagnosticQueryContext): AssistantDiagnosticResponse
}

/**
 * Sanitizes and redacts user inputs and terminal error logs before building AI context.
 * Defends against secret leakage and prompt injection.
 */
object ContextRedactor {

    private val HighRiskCommands = listOf(
        Regex("\\brm\\s+(-[a-zA-Z]*r[a-zA-Z]*f|--recursive|--force)\\s+[/~*]", RegexOption.IGNORE_CASE),
        Regex("\\bdd\\s+if=", RegexOption.IGNORE_CASE),
        Regex("\\bmkfs(\\.[a-zA-Z0-9]+)?\\b", RegexOption.IGNORE_CASE),
        Regex("\\bchmod\\s+(-R\\s+)?777\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(drop\\s+database|truncate\\s+table)\\b", RegexOption.IGNORE_CASE)
    )

    fun sanitizeAndRedact(rawText: String): String {
        val withoutSecrets = SecretGuardValidator.redact(rawText)
        // Strip system-level prompt injection markers if present
        return withoutSecrets
            .replace("```", "")
            .take(4000)
    }

    fun detectHighRiskCommand(command: String): String? {
        for (pattern in HighRiskCommands) {
            if (pattern.containsMatchIn(command)) {
                return "HIGH CAUTION: This command contains potentially destructive actions (e.g., recursive deletion, disk formatting, or broad permission reset). Never run without explicit backup."
            }
        }
        return null
    }

    fun buildPreviewPrompt(context: DiagnosticQueryContext): String {
        val sanitizedQuestion = sanitizeAndRedact(context.userQuestion)
        val sanitizedError = context.errorSnippet?.let { sanitizeAndRedact(it) } ?: "None"
        val env = context.selectedEnvironment?.badgeLabel ?: "All Environments"

        val relevantRecipes = context.availableVerifiedRecipes
            .filter { context.selectedEnvironment == null || it.environmentType == context.selectedEnvironment }
            .take(3)
            .joinToString("\n") { "- [${it.environmentType.badgeLabel}] ${it.title}: `${it.commandSyntax}`" }

        return """
            === NEXUS Context Preview (Inspect before sending) ===
            Target Environment Scope: $env
            User Question: $sanitizedQuestion
            Observed Error Log: $sanitizedError
            
            Retrieved Saved Verified Recipes:
            ${if (relevantRecipes.isNotBlank()) relevantRecipes else "No local verified matches"}
            =====================================================
        """.trimIndent()
    }
}
