package com.zakshub.nexus.ai

import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.ImpactLevel
import com.zakshub.nexus.domain.model.VerificationStatus

/**
 * 100% Offline, deterministic retrieval provider that powers the NEXUS assistant
 * without network access, third-party API keys, or cloud dependencies.
 *
 * It searches the owner's saved verified recipes and known troubleshooting cases first,
 * providing grounded answers with direct citations.
 */
class OfflineRetrievalProvider : AiAssistantProvider {

    override val providerName: String = "Offline Personal Vault Engine"
    override val isOfflineOnly: Boolean = true

    override suspend fun diagnose(context: DiagnosticQueryContext): AssistantDiagnosticResponse {
        val q = context.userQuestion.lowercase().trim()
        val err = context.errorSnippet?.lowercase()?.trim().orEmpty()
        val combined = "$q $err"

        // 1. Check known troubleshooting cases
        val matchedCase = context.knownCases.firstOrNull { c ->
            (context.selectedEnvironment == null || c.environmentType == context.selectedEnvironment) &&
            (combined.contains(c.symptomTitle.lowercase()) ||
             (c.exactErrorSnippet.isNotBlank() && combined.contains(c.exactErrorSnippet.lowercase())) ||
             (c.symptomTitle.lowercase().split(" ").any { word -> word.length > 3 && combined.contains(word) }))
        }

        if (matchedCase != null) {
            val highRisk = ContextRedactor.detectHighRiskCommand(matchedCase.verifiedFixCommand)
            return AssistantDiagnosticResponse(
                plainEnglishDiagnosis = "Identified matching troubleshooting case: ${matchedCase.plainEnglishWhyItHappens}",
                suggestedCommand = matchedCase.verifiedFixCommand,
                targetEnvironment = matchedCase.environmentType,
                impactLevel = if (highRisk != null) ImpactLevel.HIGH_CAUTION else ImpactLevel.CONFIG_CHANGE,
                verificationStatus = matchedCase.verificationStatus,
                citations = listOf(
                    AssistantCitation(
                        recordId = matchedCase.id,
                        title = matchedCase.symptomTitle,
                        environmentType = matchedCase.environmentType.badgeLabel,
                        relevanceSnippet = "Matches reported symptom: '${matchedCase.exactErrorSnippet}'"
                    )
                ),
                highRiskWarning = highRisk,
                isGeneratedByAi = false
            )
        }

        // 2. Check saved recipes for matching intent
        val matchedRecipe = context.availableVerifiedRecipes.firstOrNull { r ->
            (context.selectedEnvironment == null || r.environmentType == context.selectedEnvironment) &&
            (combined.contains(r.title.lowercase()) ||
             r.purposePlainEnglish.lowercase().split(" ").count { word -> word.length > 3 && combined.contains(word) } >= 2 ||
             (r.commandSyntax.lowercase().split(" ").any { cmd -> cmd.length > 3 && combined.contains(cmd) }))
        }

        if (matchedRecipe != null) {
            val highRisk = ContextRedactor.detectHighRiskCommand(matchedRecipe.commandSyntax)
            return AssistantDiagnosticResponse(
                plainEnglishDiagnosis = "Found verified recipe in your personal memory: ${matchedRecipe.purposePlainEnglish}",
                suggestedCommand = matchedRecipe.commandSyntax,
                targetEnvironment = matchedRecipe.environmentType,
                impactLevel = matchedRecipe.impactLevel,
                verificationStatus = matchedRecipe.verificationStatus,
                citations = listOf(
                    AssistantCitation(
                        recordId = matchedRecipe.id,
                        title = matchedRecipe.title,
                        environmentType = matchedRecipe.environmentType.badgeLabel,
                        relevanceSnippet = matchedRecipe.expectedOutputPlainEnglish
                    )
                ),
                highRiskWarning = highRisk,
                isGeneratedByAi = false
            )
        }

        // 3. Fallback deterministic guidance if no exact match is found
        val fallbackEnv = context.selectedEnvironment ?: ExecutionEnvironmentType.TERMUX_HOST
        return AssistantDiagnosticResponse(
            plainEnglishDiagnosis = "No exact verified solution found in your saved notes for '$q'. To stay safe, first verify your active environment before trying new commands.",
            suggestedCommand = if (fallbackEnv == ExecutionEnvironmentType.DEBIAN_PROOT) "cat /etc/os-release" else "termux-wake-lock && whoami",
            targetEnvironment = fallbackEnv,
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.AI_SUGGESTED,
            citations = emptyList(),
            highRiskWarning = null,
            isGeneratedByAi = false
        )
    }
}
