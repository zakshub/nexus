package com.zakshub.nexus.security

/**
 * Deterministic client-side validator that prevents passwords, private keys,
 * and API tokens from being saved in ordinary notes, exported files, or AI prompts.
 *
 * Implements ADR 0009 ("Client-Side Secret Guard").
 */
object SecretGuardValidator {

    data class ValidationResult(
        val isSafe: Boolean,
        val findings: List<SecretFinding>
    )

    data class SecretFinding(
        val ruleName: String,
        val userFriendlyWarning: String,
        val safePlaceholderSuggestion: String
    )

    private data class DetectionRule(
        val name: String,
        val pattern: Regex,
        val warning: String,
        val placeholder: String
    )

    private val rules = listOf(
        DetectionRule(
            name = "PEM / OpenSSH Private Key",
            pattern = Regex("-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----", RegexOption.IGNORE_CASE),
            warning = "This text contains a raw Private Key. Private keys must never be stored in notes.",
            placeholder = "<YOUR_SSH_PRIVATE_KEY_FILE_PATH>"
        ),
        DetectionRule(
            name = "GitHub Personal Access Token",
            pattern = Regex("\\b(ghp_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|gho_[A-Za-z0-9]{20,})\\b"),
            warning = "This text contains a live GitHub access token.",
            placeholder = "<YOUR_GITHUB_TOKEN>"
        ),
        DetectionRule(
            name = "Google / Gemini API Key",
            pattern = Regex("\\bAIza[0-9A-Za-z\\-_]{30,}\\b"),
            warning = "This text contains a Google or Gemini API key.",
            placeholder = "<YOUR_GEMINI_API_KEY>"
        ),
        DetectionRule(
            name = "API Secret Key (sk-)",
            pattern = Regex("\\bsk-[A-Za-z0-9\\-_]{20,}\\b"),
            warning = "This text contains an API secret key starting with 'sk-'.",
            placeholder = "<YOUR_API_SECRET_KEY>"
        ),
        DetectionRule(
            name = "Bearer Authorization Token",
            pattern = Regex("Authorization:\\s*Bearer\\s+[A-Za-z0-9\\-._~+/]+=*", RegexOption.IGNORE_CASE),
            warning = "This text contains a Bearer authentication token.",
            placeholder = "Authorization: Bearer <REDACTED_TOKEN>"
        ),
        DetectionRule(
            name = "Inline Password Assignment",
            pattern = Regex("\\b(password|passwd|pwd|api_secret)\\s*[:=]\\s*['\"]?[^\\s'\">]{4,}['\"]?", RegexOption.IGNORE_CASE),
            warning = "This text appears to contain a hardcoded password or secret value.",
            placeholder = "password=<ENTER_PASSWORD_WHEN_PROMPTED>"
        )
    )

    /**
     * Inspects candidate text and returns whether it is safe to store in notes/recipes.
     */
    fun validate(text: String): ValidationResult {
        if (text.isBlank()) {
            return ValidationResult(isSafe = true, findings = emptyList())
        }
        val matches = rules.mapNotNull { rule ->
            if (rule.pattern.containsMatchIn(text)) {
                SecretFinding(
                    ruleName = rule.name,
                    userFriendlyWarning = rule.warning,
                    safePlaceholderSuggestion = rule.placeholder
                )
            } else {
                null
            }
        }
        return ValidationResult(
            isSafe = matches.isEmpty(),
            findings = matches
        )
    }

    /**
     * Redacts any detected secrets from text before copying logs or preparing context.
     */
    fun redact(text: String): String {
        var sanitized = text
        for (rule in rules) {
            sanitized = rule.pattern.replace(sanitized, rule.placeholder)
        }
        return sanitized
    }
}
