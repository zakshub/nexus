package com.zakshub.nexus.data.backup

import com.zakshub.nexus.data.local.NexusDatabase
import com.zakshub.nexus.data.local.entity.DeviceEntity
import com.zakshub.nexus.data.local.entity.EnvironmentEntity
import com.zakshub.nexus.data.local.entity.RecipeEntity
import com.zakshub.nexus.data.local.entity.TroubleshootingCaseEntity
import com.zakshub.nexus.data.local.entity.WorkflowEntity
import com.zakshub.nexus.data.local.entity.WorkflowStepEntity
import com.zakshub.nexus.security.SecretGuardValidator
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class DeviceDto(
    val id: String,
    val name: String,
    val platform: String,
    val hardwareSummary: String,
    val roleDescription: String,
    val isVerified: Boolean
)

@Serializable
data class EnvironmentDto(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val type: String,
    val shellSummary: String,
    val filesystemBoundaryNote: String,
    val commonPitfallWarning: String
)

@Serializable
data class RecipeDto(
    val id: String,
    val title: String,
    val targetDeviceName: String,
    val targetPlatform: String,
    val environmentType: String,
    val purposePlainEnglish: String,
    val commandSyntax: String,
    val parametersJson: String,
    val expectedOutputPlainEnglish: String,
    val impactLevel: String,
    val verificationStatus: String,
    val rollbackOrUndoHint: String? = null,
    val isPinnedDaily: Boolean = false
)

@Serializable
data class CaseDto(
    val id: String,
    val symptomTitle: String,
    val exactErrorSnippet: String,
    val targetDeviceName: String,
    val environmentType: String,
    val plainEnglishWhyItHappens: String,
    val safeReadOnlyCheckCommand: String,
    val verifiedFixCommand: String,
    val verificationStatus: String
)

@Serializable
data class WorkflowStepDto(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val targetDeviceName: String,
    val environmentType: String,
    val explanationPlainEnglish: String,
    val commandToRun: String,
    val expectedResult: String,
    val impactLevel: String,
    val ifItFailsHint: String,
    val isCompleted: Boolean
)

@Serializable
data class WorkflowDto(
    val id: String,
    val title: String,
    val summaryPlainEnglish: String,
    val activeStepNumber: Int,
    val isCompleted: Boolean,
    val steps: List<WorkflowStepDto>
)

@Serializable
data class NexusBackupPayload(
    val version: Int = 1,
    val exportedAt: String,
    val devices: List<DeviceDto>,
    val environments: List<EnvironmentDto>,
    val recipes: List<RecipeDto>,
    val troubleshootingCases: List<CaseDto>,
    val workflows: List<WorkflowDto>
)

sealed class ImportResult {
    data class Success(
        val devicesCount: Int,
        val environmentsCount: Int,
        val recipesCount: Int,
        val casesCount: Int,
        val workflowsCount: Int
    ) : ImportResult()

    data class BlockedBySecretGuard(val violationSummary: String) : ImportResult()
    data class Failure(val errorMessage: String) : ImportResult()
}

object NexusBackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportToJson(database: NexusDatabase): String {
        val devices = database.deviceDao().getAllDevices().map {
            DeviceDto(it.id, it.name, it.platform, it.hardwareSummary, it.roleDescription, it.isVerified)
        }
        val envs = database.environmentDao().getAllEnvironments().map {
            EnvironmentDto(it.id, it.deviceId, it.deviceName, it.type, it.shellSummary, it.filesystemBoundaryNote, it.commonPitfallWarning)
        }
        val recipes = database.recipeDao().getAllRecipes().map {
            RecipeDto(
                id = it.id,
                title = it.title,
                targetDeviceName = it.targetDeviceName,
                targetPlatform = it.targetPlatform,
                environmentType = it.environmentType,
                purposePlainEnglish = it.purposePlainEnglish,
                commandSyntax = it.commandSyntax,
                parametersJson = it.parametersJson,
                expectedOutputPlainEnglish = it.expectedOutputPlainEnglish,
                impactLevel = it.impactLevel,
                verificationStatus = it.verificationStatus,
                rollbackOrUndoHint = it.rollbackOrUndoHint,
                isPinnedDaily = it.isPinnedDaily
            )
        }
        val cases = database.troubleshootingDao().getAllCases().map {
            CaseDto(it.id, it.symptomTitle, it.exactErrorSnippet, it.targetDeviceName, it.environmentType, it.plainEnglishWhyItHappens, it.safeReadOnlyCheckCommand, it.verifiedFixCommand, it.verificationStatus)
        }
        val workflows = database.workflowDao().getAllWorkflowsWithSteps().map { ws ->
            WorkflowDto(
                id = ws.workflow.id,
                title = ws.workflow.title,
                summaryPlainEnglish = ws.workflow.summaryPlainEnglish,
                activeStepNumber = ws.workflow.activeStepNumber,
                isCompleted = ws.workflow.isCompleted,
                steps = ws.steps.map { s ->
                    WorkflowStepDto(s.id, s.stepNumber, s.title, s.targetDeviceName, s.environmentType, s.explanationPlainEnglish, s.commandToRun, s.expectedResult, s.impactLevel, s.ifItFailsHint, s.isCompleted)
                }
            )
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val payload = NexusBackupPayload(
            version = 1,
            exportedAt = dateStr,
            devices = devices,
            environments = envs,
            recipes = recipes,
            troubleshootingCases = cases,
            workflows = workflows
        )

        return json.encodeToString(payload)
    }

    suspend fun exportToMarkdown(database: NexusDatabase): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val sb = StringBuilder()
        sb.appendLine("# NEXUS Technical Memory Notebook")
        sb.appendLine("*Exported on $dateStr*")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        sb.appendLine("## 1. Devices & Environments")
        for (env in database.environmentDao().getAllEnvironments()) {
            sb.appendLine("### ${env.deviceName} · ${env.type}")
            sb.appendLine("- **Shell:** ${env.shellSummary}")
            sb.appendLine("- **Boundary:** ${env.filesystemBoundaryNote}")
            sb.appendLine("- **Rule:** ${env.commonPitfallWarning}")
            sb.appendLine()
        }

        sb.appendLine("## 2. Command Recipes")
        for (recipe in database.recipeDao().getAllRecipes()) {
            sb.appendLine("### ${recipe.title}")
            sb.appendLine("- **Device:** ${recipe.targetDeviceName}")
            sb.appendLine("- **Environment:** ${recipe.environmentType}")
            sb.appendLine("- **Impact:** ${recipe.impactLevel} | **Status:** ${recipe.verificationStatus}")
            sb.appendLine("- **Plain English:** ${recipe.purposePlainEnglish}")
            sb.appendLine("```bash")
            sb.appendLine(recipe.commandSyntax)
            sb.appendLine("```")
            sb.appendLine("- **Expected Result:** ${recipe.expectedOutputPlainEnglish}")
            recipe.rollbackOrUndoHint?.let { sb.appendLine("- **Undo:** $it") }
            sb.appendLine()
        }

        sb.appendLine("## 3. Troubleshooting Cases")
        for (c in database.troubleshootingDao().getAllCases()) {
            sb.appendLine("### ${c.symptomTitle}")
            sb.appendLine("- **Environment:** ${c.targetDeviceName} (${c.environmentType})")
            sb.appendLine("- **Error Seen:** `${c.exactErrorSnippet}`")
            sb.appendLine("- **Cause:** ${c.plainEnglishWhyItHappens}")
            sb.appendLine("- **Safe Check:** `${c.safeReadOnlyCheckCommand}`")
            sb.appendLine("- **Verified Fix:** `${c.verifiedFixCommand}`")
            sb.appendLine()
        }

        return sb.toString()
    }

    suspend fun importFromJson(jsonString: String, database: NexusDatabase): ImportResult {
        val validation = SecretGuardValidator.validate(jsonString)
        if (!validation.isSafe) {
            val summary = validation.findings.joinToString("; ") { "${it.ruleName}: ${it.userFriendlyWarning}" }
            return ImportResult.BlockedBySecretGuard("Import rejected for your protection: $summary")
        }

        return try {
            val payload = json.decodeFromString<NexusBackupPayload>(jsonString)

            val deviceEntities = payload.devices.map {
                DeviceEntity(it.id, it.name, it.platform, it.hardwareSummary, it.roleDescription, it.isVerified)
            }
            database.deviceDao().upsertDevices(deviceEntities)

            val envEntities = payload.environments.map {
                EnvironmentEntity(it.id, it.deviceId, it.deviceName, it.type, it.shellSummary, it.filesystemBoundaryNote, it.commonPitfallWarning)
            }
            database.environmentDao().upsertEnvironments(envEntities)

            val recipeEntities = payload.recipes.map {
                RecipeEntity(
                    id = it.id,
                    title = it.title,
                    targetDeviceName = it.targetDeviceName,
                    targetPlatform = it.targetPlatform,
                    environmentType = it.environmentType,
                    purposePlainEnglish = it.purposePlainEnglish,
                    commandSyntax = it.commandSyntax,
                    parametersJson = it.parametersJson,
                    expectedOutputPlainEnglish = it.expectedOutputPlainEnglish,
                    impactLevel = it.impactLevel,
                    verificationStatus = it.verificationStatus,
                    rollbackOrUndoHint = it.rollbackOrUndoHint,
                    isPinnedDaily = it.isPinnedDaily
                )
            }
            database.recipeDao().upsertRecipes(recipeEntities)

            val caseEntities = payload.troubleshootingCases.map {
                TroubleshootingCaseEntity(it.id, it.symptomTitle, it.exactErrorSnippet, it.targetDeviceName, it.environmentType, it.plainEnglishWhyItHappens, it.safeReadOnlyCheckCommand, it.verifiedFixCommand, it.verificationStatus)
            }
            database.troubleshootingDao().upsertCases(caseEntities)

            val workflowEntities = payload.workflows.map {
                WorkflowEntity(it.id, it.title, it.summaryPlainEnglish, it.activeStepNumber, it.isCompleted)
            }
            database.workflowDao().upsertWorkflows(workflowEntities)

            val stepEntities = payload.workflows.flatMap { wf ->
                wf.steps.map { s ->
                    WorkflowStepEntity(s.id, wf.id, s.stepNumber, s.title, s.targetDeviceName, s.environmentType, s.explanationPlainEnglish, s.commandToRun, s.expectedResult, s.impactLevel, s.ifItFailsHint, s.isCompleted)
                }
            }
            database.workflowDao().upsertWorkflowSteps(stepEntities)

            ImportResult.Success(
                devicesCount = deviceEntities.size,
                environmentsCount = envEntities.size,
                recipesCount = recipeEntities.size,
                casesCount = caseEntities.size,
                workflowsCount = workflowEntities.size
            )
        } catch (e: Exception) {
            ImportResult.Failure("Failed to parse backup JSON: ${e.localizedMessage}")
        }
    }
}
