package com.zakshub.nexus

import com.zakshub.nexus.ai.ContextRedactor
import com.zakshub.nexus.ai.DiagnosticQueryContext
import com.zakshub.nexus.ai.OfflineRetrievalProvider
import com.zakshub.nexus.data.StarterKnowledgeCatalog
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.ImpactLevel
import com.zakshub.nexus.domain.model.VerificationStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAssistantTest {

    private val provider = OfflineRetrievalProvider()

    @Test
    fun `diagnose matches known troubleshooting case and provides verified citation`() = runBlocking {
        val context = DiagnosticQueryContext(
            userQuestion = "My SSH connection timed out on port 8022",
            errorSnippet = "Connection timed out",
            selectedEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
            availableVerifiedRecipes = StarterKnowledgeCatalog.recipes,
            knownCases = StarterKnowledgeCatalog.troubleshootingCases
        )

        val response = provider.diagnose(context)

        assertNotNull(response.suggestedCommand)
        assertTrue(response.suggestedCommand!!.contains("sshd") || response.suggestedCommand!!.contains("logcat"))
        assertEquals(ExecutionEnvironmentType.TERMUX_HOST, response.targetEnvironment)
        assertEquals(VerificationStatus.VERIFIED, response.verificationStatus)
        assertEquals(1, response.citations.size)
        assertEquals(ExecutionEnvironmentType.TERMUX_HOST.badgeLabel, response.citations.first().environmentType)
        assertFalse(response.isGeneratedByAi)
    }

    @Test
    fun `diagnose matches saved verified recipe when searching for recipe intent`() = runBlocking {
        val context = DiagnosticQueryContext(
            userQuestion = "How do I enter my Debian proot container?",
            errorSnippet = null,
            selectedEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
            availableVerifiedRecipes = StarterKnowledgeCatalog.recipes,
            knownCases = StarterKnowledgeCatalog.troubleshootingCases
        )

        val response = provider.diagnose(context)

        assertNotNull(response.suggestedCommand)
        assertTrue(response.suggestedCommand!!.contains("proot-distro login debian"))
        assertEquals(ExecutionEnvironmentType.TERMUX_HOST, response.targetEnvironment)
        assertEquals(1, response.citations.size)
        assertEquals(VerificationStatus.VERIFIED, response.verificationStatus)
    }

    @Test
    fun `diagnose returns safe deterministic fallback when no matches exist`() = runBlocking {
        val context = DiagnosticQueryContext(
            userQuestion = "unrelated completely unknown question xyz123",
            errorSnippet = null,
            selectedEnvironment = ExecutionEnvironmentType.DEBIAN_PROOT,
            availableVerifiedRecipes = StarterKnowledgeCatalog.recipes,
            knownCases = StarterKnowledgeCatalog.troubleshootingCases
        )

        val response = provider.diagnose(context)

        assertNotNull(response.suggestedCommand)
        assertTrue(response.suggestedCommand!!.contains("cat /etc/os-release"))
        assertEquals(ExecutionEnvironmentType.DEBIAN_PROOT, response.targetEnvironment)
        assertEquals(ImpactLevel.READ_ONLY, response.impactLevel)
        assertEquals(VerificationStatus.AI_SUGGESTED, response.verificationStatus)
        assertTrue(response.citations.isEmpty())
    }

    @Test
    fun `context redactor strips secrets and sensitive keys from queries and errors`() {
        val rawInput = "My server error with token ghp_1234567890abcdef1234567890abcdef1234 and password=\"supersecret\""
        val redacted = ContextRedactor.sanitizeAndRedact(rawInput)

        assertFalse(redacted.contains("ghp_1234567890abcdef1234567890abcdef1234"))
        assertFalse(redacted.contains("supersecret"))
        assertTrue(redacted.contains("<YOUR_GITHUB_TOKEN>"))
        assertTrue(redacted.contains("<ENTER_PASSWORD_WHEN_PROMPTED>"))
    }

    @Test
    fun `context redactor detects destructive and dangerous high-risk commands`() {
        val dangerousCommands = listOf(
            "rm -rf /",
            "rm -rf *",
            "mkfs.ext4 /dev/sda1",
            "chmod -R 777 /",
            "dd if=/dev/zero of=/dev/sda",
            "drop database production;"
        )

        dangerousCommands.forEach { cmd ->
            val warning = ContextRedactor.detectHighRiskCommand(cmd)
            assertNotNull("Expected high risk detection for '$cmd'", warning)
            assertTrue(warning!!.contains("HIGH CAUTION"))
        }

        val safeCommands = listOf(
            "cat /etc/os-release",
            "pkg update && pkg upgrade",
            "sshd -p 8022",
            "ls -la"
        )

        safeCommands.forEach { cmd ->
            val warning = ContextRedactor.detectHighRiskCommand(cmd)
            assertNull("Expected no high risk detection for safe command '$cmd'", warning)
        }
    }

    @Test
    fun `context redactor builds preview prompt with environment badge and sanitized logs`() {
        val context = DiagnosticQueryContext(
            userQuestion = "Help with secret token ghp_abcdef1234567890abcdef12345678901234",
            errorSnippet = "Failed with password: MySecretPassword123",
            selectedEnvironment = ExecutionEnvironmentType.TERMUX_HOST,
            availableVerifiedRecipes = StarterKnowledgeCatalog.recipes.take(2)
        )

        val preview = ContextRedactor.buildPreviewPrompt(context)

        assertTrue(preview.contains("=== NEXUS Context Preview"))
        assertTrue(preview.contains(ExecutionEnvironmentType.TERMUX_HOST.badgeLabel))
        assertFalse(preview.contains("ghp_abcdef1234567890abcdef12345678901234"))
        assertFalse(preview.contains("MySecretPassword123"))
        assertTrue(preview.contains("<YOUR_GITHUB_TOKEN>"))
    }
}
