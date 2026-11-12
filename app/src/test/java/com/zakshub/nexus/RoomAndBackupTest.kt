package com.zakshub.nexus

import com.zakshub.nexus.data.StarterKnowledgeCatalog
import com.zakshub.nexus.data.backup.DeviceDto
import com.zakshub.nexus.data.backup.EnvironmentDto
import com.zakshub.nexus.data.backup.ImportResult
import com.zakshub.nexus.data.backup.NexusBackupManager
import com.zakshub.nexus.data.backup.NexusBackupPayload
import com.zakshub.nexus.data.backup.RecipeDto
import com.zakshub.nexus.security.SecretGuardValidator
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomAndBackupTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun `starter catalog has verified devices and environments`() {
        assertEquals(2, StarterKnowledgeCatalog.devices.size)
        assertEquals(3, StarterKnowledgeCatalog.environments.size)
        assertTrue(StarterKnowledgeCatalog.recipes.size >= 5)

        val pixel = StarterKnowledgeCatalog.devices.first { it.id == "device-pixel7pro" }
        assertEquals("Google Pixel 7 Pro", pixel.name)
        assertTrue(pixel.isVerified)

        val termuxEnv = StarterKnowledgeCatalog.environments.first { it.id == "env-termux-host" }
        val debianEnv = StarterKnowledgeCatalog.environments.first { it.id == "env-debian-proot" }
        assertFalse(termuxEnv.type == debianEnv.type)
    }

    @Test
    fun `backup payload serializes and deserializes cleanly`() {
        val payload = NexusBackupPayload(
            version = 1,
            exportedAt = "2026-11-12T11:00:00Z",
            devices = listOf(
                DeviceDto("d1", "Test Pixel", "ANDROID", "12GB RAM", "Primary", true)
            ),
            environments = listOf(
                EnvironmentDto("e1", "d1", "Test Pixel", "TERMUX_HOST", "Termux Shell", "Boundary", "Warning")
            ),
            recipes = listOf(
                RecipeDto(
                    id = "r1",
                    title = "Start SSH",
                    targetDeviceName = "Test Pixel",
                    targetPlatform = "ANDROID",
                    environmentType = "TERMUX_HOST",
                    purposePlainEnglish = "Starts SSH server",
                    commandSyntax = "termux-wake-lock && sshd",
                    parametersJson = "[]",
                    expectedOutputPlainEnglish = "Notification appears",
                    impactLevel = "CONFIG_CHANGE",
                    verificationStatus = "VERIFIED"
                )
            ),
            troubleshootingCases = emptyList(),
            workflows = emptyList()
        )

        val encoded = json.encodeToString(payload)
        assertTrue(encoded.contains("Start SSH"))
        assertTrue(encoded.contains("termux-wake-lock && sshd"))

        val decoded = json.decodeFromString<NexusBackupPayload>(encoded)
        assertEquals(1, decoded.devices.size)
        assertEquals(1, decoded.environments.size)
        assertEquals(1, decoded.recipes.size)
        assertEquals("Start SSH", decoded.recipes.first().title)
    }

    @Test
    fun `importing payload with leaked SSH private key is blocked by SecretGuard`() {
        val maliciousJson = """
            {
                "version": 1,
                "exportedAt": "2026-11-12T11:00:00Z",
                "devices": [],
                "environments": [],
                "recipes": [
                    {
                        "id": "bad-recipe",
                        "title": "Leaked Key Recipe",
                        "targetDeviceName": "Pixel",
                        "targetPlatform": "ANDROID",
                        "environmentType": "TERMUX_HOST",
                        "purposePlainEnglish": "Contains leaked secret",
                        "commandSyntax": "echo '-----BEGIN RSA PRIVATE KEY-----\nMIIE...' > ~/.ssh/id_rsa",
                        "parametersJson": "[]",
                        "expectedOutputPlainEnglish": "Done",
                        "impactLevel": "HIGH_CAUTION",
                        "verificationStatus": "UNVERIFIED_DRAFT"
                    }
                ],
                "troubleshootingCases": [],
                "workflows": []
            }
        """.trimIndent()

        val validation = SecretGuardValidator.validate(maliciousJson)
        assertFalse(validation.isSafe)
        assertTrue(validation.findings.any { it.ruleName.contains("Private Key") })
    }

    @Test
    fun `importing payload with leaked GitHub token is blocked by SecretGuard`() {
        val token = "ghp_" + "1234567890abcdefghij12345678"
        val payload = """
            {
                "version": 1,
                "exportedAt": "2026-11-12T11:00:00Z",
                "devices": [],
                "environments": [],
                "recipes": [{
                    "id": "r2",
                    "title": "Git push with token",
                    "targetDeviceName": "Pixel",
                    "targetPlatform": "ANDROID",
                    "environmentType": "TERMUX_HOST",
                    "purposePlainEnglish": "Unsafe token",
                    "commandSyntax": "git push https://$token@github.com/repo.git",
                    "parametersJson": "[]",
                    "expectedOutputPlainEnglish": "Pushed",
                    "impactLevel": "HIGH_CAUTION",
                    "verificationStatus": "UNVERIFIED_DRAFT"
                }],
                "troubleshootingCases": [],
                "workflows": []
            }
        """.trimIndent()

        val validation = SecretGuardValidator.validate(payload)
        assertFalse(validation.isSafe)
        assertTrue(validation.findings.any { it.ruleName.contains("GitHub") })
    }
}
