package com.zakshub.nexus

import com.zakshub.nexus.data.StarterKnowledgeCatalog
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchAndWorkflowTest {

    @Test
    fun `search filter matches titles, commands, and keywords regardless of case`() {
        val query = "ssh"
        val results = StarterKnowledgeCatalog.recipes.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.purposePlainEnglish.contains(query, ignoreCase = true) ||
            it.commandSyntax.contains(query, ignoreCase = true)
        }
        assertTrue("Expected search for 'ssh' to return matches", results.isNotEmpty())
        assertTrue(results.any { it.commandSyntax.contains("8022") })
    }

    @Test
    fun `environment filtering isolates Termux recipes from Debian proot recipes`() {
        val allRecipes = StarterKnowledgeCatalog.recipes
        val termuxOnly = allRecipes.filter { it.environmentType == ExecutionEnvironmentType.TERMUX_HOST }
        val debianOnly = allRecipes.filter { it.environmentType == ExecutionEnvironmentType.DEBIAN_PROOT }

        assertTrue(termuxOnly.isNotEmpty())
        assertTrue(debianOnly.isNotEmpty())

        termuxOnly.forEach { recipe ->
            assertEquals(ExecutionEnvironmentType.TERMUX_HOST, recipe.environmentType)
        }
        debianOnly.forEach { recipe ->
            assertEquals(ExecutionEnvironmentType.DEBIAN_PROOT, recipe.environmentType)
        }
    }

    @Test
    fun `verification status transitions cleanly between Verified and Unverified`() {
        val recipe = StarterKnowledgeCatalog.recipes.first()
        assertEquals(VerificationStatus.VERIFIED, recipe.verificationStatus)

        val unverifiedCopy = recipe.copy(verificationStatus = VerificationStatus.UNVERIFIED_DRAFT)
        assertEquals(VerificationStatus.UNVERIFIED_DRAFT, unverifiedCopy.verificationStatus)

        val verifiedAgain = unverifiedCopy.copy(verificationStatus = VerificationStatus.VERIFIED)
        assertEquals(VerificationStatus.VERIFIED, verifiedAgain.verificationStatus)
    }

    @Test
    fun `guided workflow tracks step completion count and identifies active step`() {
        val workflow = StarterKnowledgeCatalog.workflows.first()
        assertEquals(0, workflow.completedStepsCount)
        assertEquals(1, workflow.nextIncompleteStepNumber)

        val updatedSteps = workflow.steps.mapIndexed { index, step ->
            if (index == 0) step.copy(isCompleted = true) else step
        }
        val progressingWorkflow = workflow.copy(steps = updatedSteps)

        assertEquals(1, progressingWorkflow.completedStepsCount)
        assertEquals(2, progressingWorkflow.nextIncompleteStepNumber)
        assertFalse(progressingWorkflow.steps.all { it.isCompleted })
    }

    @Test
    fun `troubleshooting catalog contains safe read-only check and verified fix`() {
        val cases = StarterKnowledgeCatalog.troubleshootingCases
        assertTrue(cases.isNotEmpty())

        val sshCase = cases.firstOrNull { it.id == "case-ssh-connection-refused" }
        assertNotNull(sshCase)
        assertEquals("pgrep -l sshd", sshCase?.safeReadOnlyCheckCommand)
        assertEquals("termux-wake-lock && sshd", sshCase?.verifiedFixCommand)
    }

    @Test
    fun `pinned daily recipes include startup sequence and local server`() {
        val pinned = StarterKnowledgeCatalog.recipes.filter { it.isPinnedDaily }
        assertTrue("Expected at least 3 pinned daily recipes", pinned.size >= 3)
        assertTrue(pinned.any { it.commandSyntax.contains("sshd") })
        assertTrue(pinned.any { it.commandSyntax.contains("wlan0") })
        assertTrue(pinned.any { it.commandSyntax.contains("http.server") })
    }

    @Test
    fun `expanded catalog contains RAM check, storage clean, and git sync recipes`() {
        val recipes = StarterKnowledgeCatalog.recipes
        assertTrue(recipes.any { it.id == "recipe-phone-ram-check" && it.commandSyntax == "free -h" })
        assertTrue(recipes.any { it.id == "recipe-termux-storage-clean" && it.commandSyntax.contains("pkg clean") })
        assertTrue(recipes.any { it.id == "recipe-git-safe-sync" && it.commandSyntax.contains("git status -s") })
    }

    @Test
    fun `expanded troubleshooting cases contain DNS, battery killer, and git key fixes`() {
        val cases = StarterKnowledgeCatalog.troubleshootingCases
        assertTrue(cases.size >= 5)
        assertTrue(cases.any { it.id == "case-proot-dns-resolution" && it.verifiedFixCommand.contains("8.8.8.8") })
        assertTrue(cases.any { it.id == "case-battery-killer" && it.verifiedFixCommand.contains("termux-wake-lock") })
        assertTrue(cases.any { it.id == "case-git-ssh-key" && it.verifiedFixCommand.contains("ssh-add") })
    }
}
