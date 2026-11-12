package com.zakshub.nexus

import com.zakshub.nexus.data.StarterKnowledgeCatalog
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.security.SecretGuardValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretGuardAndModelTest {

    @Test
    fun `safe commands pass SecretGuardValidator without warnings`() {
        val result = SecretGuardValidator.validate("ssh -p 8022 192.168.1.45 && proot-distro login debian")
        assertTrue(result.isSafe)
        assertTrue(result.findings.isEmpty())
    }

    @Test
    fun `private keys are blocked by SecretGuardValidator`() {
        val sample = "-----BEGIN OPENSSH PRIVATE KEY-----\nAAAA...\n-----END OPENSSH PRIVATE KEY-----"
        val result = SecretGuardValidator.validate(sample)
        assertFalse(result.isSafe)
        assertEquals("PEM / OpenSSH Private Key", result.findings.first().ruleName)
    }

    @Test
    fun `github tokens and gemini api keys are blocked and redacted`() {
        val fakeGh = "ghp_" + "a".repeat(28)
        val fakeGemini = "AIza" + "B".repeat(33)
        val combined = "export TOKEN=$fakeGh KEY=$fakeGemini"

        val result = SecretGuardValidator.validate(combined)
        assertFalse(result.isSafe)
        assertEquals(2, result.findings.size)

        val redacted = SecretGuardValidator.redact(combined)
        assertFalse(redacted.contains(fakeGh))
        assertFalse(redacted.contains(fakeGemini))
        assertTrue(redacted.contains("<YOUR_GITHUB_TOKEN>"))
        assertTrue(redacted.contains("<YOUR_GEMINI_API_KEY>"))
    }

    @Test
    fun `inline password assignments are blocked`() {
        val result = SecretGuardValidator.validate("mysql --user=root password=SuperSecret123")
        assertFalse(result.isSafe)
        assertEquals("Inline Password Assignment", result.findings.first().ruleName)
    }

    @Test
    fun `Termux Host and Debian proot are strictly separated environments`() {
        assertNotEquals(
            ExecutionEnvironmentType.TERMUX_HOST,
            ExecutionEnvironmentType.DEBIAN_PROOT
        )
        assertNotEquals(
            ExecutionEnvironmentType.TERMUX_HOST.defaultPrompt,
            ExecutionEnvironmentType.DEBIAN_PROOT.defaultPrompt
        )

        val termuxRecipes = StarterKnowledgeCatalog.recipes.filter {
            it.environmentType == ExecutionEnvironmentType.TERMUX_HOST
        }
        val debianRecipes = StarterKnowledgeCatalog.recipes.filter {
            it.environmentType == ExecutionEnvironmentType.DEBIAN_PROOT
        }

        assertTrue(termuxRecipes.isNotEmpty())
        assertTrue(debianRecipes.isNotEmpty())

        // Verify no starter recipe contains raw secrets
        StarterKnowledgeCatalog.recipes.forEach { recipe ->
            assertTrue(
                "Starter recipe ${recipe.id} must be secret-free",
                SecretGuardValidator.validate(recipe.commandSyntax).isSafe
            )
        }
    }
}
