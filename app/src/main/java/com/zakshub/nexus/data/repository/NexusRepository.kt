package com.zakshub.nexus.data.repository

import com.zakshub.nexus.data.backup.ImportResult
import com.zakshub.nexus.data.backup.NexusBackupManager
import com.zakshub.nexus.data.local.NexusDatabase
import com.zakshub.nexus.data.local.entity.RecipeEntity
import com.zakshub.nexus.domain.model.CommandParameterBreakdown
import com.zakshub.nexus.domain.model.CommandRecipe
import com.zakshub.nexus.domain.model.DevicePlatform
import com.zakshub.nexus.domain.model.EnvironmentProfile
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.GuidedWorkflow
import com.zakshub.nexus.domain.model.GuidedWorkflowStep
import com.zakshub.nexus.domain.model.ImpactLevel
import com.zakshub.nexus.domain.model.TroubleshootingCase
import com.zakshub.nexus.domain.model.VerificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class NexusRepository(private val database: NexusDatabase) {

    fun getAllRecipesFlow(): Flow<List<CommandRecipe>> {
        return database.recipeDao().getAllRecipesFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getRecipesByEnvironmentFlow(envType: ExecutionEnvironmentType): Flow<List<CommandRecipe>> {
        return database.recipeDao().getRecipesByEnvironmentFlow(envType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun searchRecipesFlow(query: String): Flow<List<CommandRecipe>> {
        return database.recipeDao().searchRecipesFlow(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun upsertRecipe(recipe: CommandRecipe) {
        val paramsJson = Json.encodeToString(
            recipe.parameters.map { mapOf("token" to it.token, "meaning" to it.meaningPlainEnglish) }
        )
        val entity = RecipeEntity(
            id = recipe.id,
            title = recipe.title,
            targetDeviceName = recipe.targetDeviceName,
            targetPlatform = recipe.targetPlatform.name,
            environmentType = recipe.environmentType.name,
            purposePlainEnglish = recipe.purposePlainEnglish,
            commandSyntax = recipe.commandSyntax,
            parametersJson = paramsJson,
            expectedOutputPlainEnglish = recipe.expectedOutputPlainEnglish,
            impactLevel = recipe.impactLevel.name,
            verificationStatus = recipe.verificationStatus.name,
            rollbackOrUndoHint = recipe.rollbackOrUndoHint,
            isPinnedDaily = recipe.isPinnedDaily
        )
        database.recipeDao().upsertRecipe(entity)
    }

    suspend fun updateRecipeVerificationStatus(id: String, status: VerificationStatus) {
        database.recipeDao().updateVerificationStatus(id, status.name)
    }

    suspend fun deleteRecipe(id: String) {
        database.recipeDao().deleteRecipe(id)
    }

    fun getAllEnvironmentsFlow(): Flow<List<EnvironmentProfile>> {
        return database.environmentDao().getAllEnvironmentsFlow().map { list ->
            list.map {
                EnvironmentProfile(
                    id = it.id,
                    deviceId = it.deviceId,
                    deviceName = it.deviceName,
                    type = ExecutionEnvironmentType.valueOf(it.type),
                    shellSummary = it.shellSummary,
                    filesystemBoundaryNote = it.filesystemBoundaryNote,
                    commonPitfallWarning = it.commonPitfallWarning
                )
            }
        }
    }

    fun getAllCasesFlow(): Flow<List<TroubleshootingCase>> {
        return database.troubleshootingDao().getAllCasesFlow().map { list ->
            list.map {
                TroubleshootingCase(
                    id = it.id,
                    symptomTitle = it.symptomTitle,
                    exactErrorSnippet = it.exactErrorSnippet,
                    targetDeviceName = it.targetDeviceName,
                    environmentType = ExecutionEnvironmentType.valueOf(it.environmentType),
                    plainEnglishWhyItHappens = it.plainEnglishWhyItHappens,
                    safeReadOnlyCheckCommand = it.safeReadOnlyCheckCommand,
                    verifiedFixCommand = it.verifiedFixCommand,
                    verificationStatus = VerificationStatus.valueOf(it.verificationStatus)
                )
            }
        }
    }

    fun getAllWorkflowsFlow(): Flow<List<GuidedWorkflow>> {
        return database.workflowDao().getAllWorkflowsWithStepsFlow().map { list ->
            list.map { ws ->
                GuidedWorkflow(
                    id = ws.workflow.id,
                    title = ws.workflow.title,
                    summaryPlainEnglish = ws.workflow.summaryPlainEnglish,
                    steps = ws.steps.sortedBy { it.stepNumber }.map { s ->
                        GuidedWorkflowStep(
                            stepNumber = s.stepNumber,
                            title = s.title,
                            targetDeviceName = s.targetDeviceName,
                            environmentType = ExecutionEnvironmentType.valueOf(s.environmentType),
                            explanationPlainEnglish = s.explanationPlainEnglish,
                            commandToRun = s.commandToRun,
                            expectedResult = s.expectedResult,
                            impactLevel = ImpactLevel.valueOf(s.impactLevel),
                            ifItFailsHint = s.ifItFailsHint,
                            isCompleted = s.isCompleted
                        )
                    }
                )
            }
        }
    }

    suspend fun updateStepCompletion(workflowId: String, stepNumber: Int, isCompleted: Boolean) {
        val stepId = "$workflowId-step-$stepNumber"
        database.workflowDao().updateStepCompletion(stepId, isCompleted)
    }

    suspend fun exportJson(): String = NexusBackupManager.exportToJson(database)

    suspend fun exportMarkdown(): String = NexusBackupManager.exportToMarkdown(database)

    suspend fun importJson(jsonContent: String): ImportResult = NexusBackupManager.importFromJson(jsonContent, database)

    private fun RecipeEntity.toDomainModel(): CommandRecipe {
        val params = try {
            val jsonElement = Json.parseToJsonElement(parametersJson)
            jsonElement.jsonArray.mapNotNull { item ->
                val obj = item.jsonObject
                val token = obj["token"]?.jsonPrimitive?.content
                val meaning = obj["meaning"]?.jsonPrimitive?.content
                if (token != null && meaning != null) {
                    CommandParameterBreakdown(token, meaning)
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }

        return CommandRecipe(
            id = id,
            title = title,
            targetDeviceName = targetDeviceName,
            targetPlatform = try { DevicePlatform.valueOf(targetPlatform) } catch (_: Exception) { DevicePlatform.ANDROID },
            environmentType = try { ExecutionEnvironmentType.valueOf(environmentType) } catch (_: Exception) { ExecutionEnvironmentType.TERMUX_HOST },
            purposePlainEnglish = purposePlainEnglish,
            commandSyntax = commandSyntax,
            parameters = params,
            expectedOutputPlainEnglish = expectedOutputPlainEnglish,
            impactLevel = try { ImpactLevel.valueOf(impactLevel) } catch (_: Exception) { ImpactLevel.READ_ONLY },
            verificationStatus = try { VerificationStatus.valueOf(verificationStatus) } catch (_: Exception) { VerificationStatus.VERIFIED },
            rollbackOrUndoHint = rollbackOrUndoHint,
            isPinnedDaily = isPinnedDaily
        )
    }
}
