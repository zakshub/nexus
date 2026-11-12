package com.zakshub.nexus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zakshub.nexus.data.StarterKnowledgeCatalog
import com.zakshub.nexus.data.local.dao.DeviceDao
import com.zakshub.nexus.data.local.dao.EnvironmentDao
import com.zakshub.nexus.data.local.dao.RecipeDao
import com.zakshub.nexus.data.local.dao.TroubleshootingDao
import com.zakshub.nexus.data.local.dao.WorkflowDao
import com.zakshub.nexus.data.local.entity.DeviceEntity
import com.zakshub.nexus.data.local.entity.EnvironmentEntity
import com.zakshub.nexus.data.local.entity.RecipeEntity
import com.zakshub.nexus.data.local.entity.TroubleshootingCaseEntity
import com.zakshub.nexus.data.local.entity.WorkflowEntity
import com.zakshub.nexus.data.local.entity.WorkflowStepEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Database(
    entities = [
        DeviceEntity::class,
        EnvironmentEntity::class,
        RecipeEntity::class,
        TroubleshootingCaseEntity::class,
        WorkflowEntity::class,
        WorkflowStepEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexusDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao
    abstract fun environmentDao(): EnvironmentDao
    abstract fun recipeDao(): RecipeDao
    abstract fun troubleshootingDao(): TroubleshootingDao
    abstract fun workflowDao(): WorkflowDao

    companion object {
        @Volatile
        private var INSTANCE: NexusDatabase? = null

        fun getInstance(context: Context, coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NexusDatabase::class.java,
                    "nexus_vault.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        coroutineScope.launch {
                            INSTANCE?.preSeedDatabase()
                        }
                    }
                }).build().also { INSTANCE = it }
            }
        }
    }

    suspend fun preSeedDatabase() {
        val deviceEntities = StarterKnowledgeCatalog.devices.map {
            DeviceEntity(
                id = it.id,
                name = it.name,
                platform = it.platform.name,
                hardwareSummary = it.hardwareSummary,
                roleDescription = it.roleDescription,
                isVerified = it.isVerified
            )
        }
        deviceDao().upsertDevices(deviceEntities)

        val envEntities = StarterKnowledgeCatalog.environments.map {
            EnvironmentEntity(
                id = it.id,
                deviceId = it.deviceId,
                deviceName = it.deviceName,
                type = it.type.name,
                shellSummary = it.shellSummary,
                filesystemBoundaryNote = it.filesystemBoundaryNote,
                commonPitfallWarning = it.commonPitfallWarning
            )
        }
        environmentDao().upsertEnvironments(envEntities)

        val recipeEntities = StarterKnowledgeCatalog.recipes.map { recipe ->
            val paramsJson = Json.encodeToString(recipe.parameters.map { mapOf("token" to it.token, "meaning" to it.meaningPlainEnglish) })
            RecipeEntity(
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
        }
        recipeDao().upsertRecipes(recipeEntities)

        val caseEntities = StarterKnowledgeCatalog.troubleshootingCases.map {
            TroubleshootingCaseEntity(
                id = it.id,
                symptomTitle = it.symptomTitle,
                exactErrorSnippet = it.exactErrorSnippet,
                targetDeviceName = it.targetDeviceName,
                environmentType = it.environmentType.name,
                plainEnglishWhyItHappens = it.plainEnglishWhyItHappens,
                safeReadOnlyCheckCommand = it.safeReadOnlyCheckCommand,
                verifiedFixCommand = it.verifiedFixCommand,
                verificationStatus = it.verificationStatus.name
            )
        }
        troubleshootingDao().upsertCases(caseEntities)

        val workflow = StarterKnowledgeCatalog.workflows.first()
        val workflowEntity = WorkflowEntity(
            id = workflow.id,
            title = workflow.title,
            summaryPlainEnglish = workflow.summaryPlainEnglish,
            activeStepNumber = 1,
            isCompleted = false
        )
        workflowDao().upsertWorkflows(listOf(workflowEntity))

        val stepEntities = workflow.steps.map { step ->
            WorkflowStepEntity(
                id = "${workflow.id}-step-${step.stepNumber}",
                workflowId = workflow.id,
                stepNumber = step.stepNumber,
                title = step.title,
                targetDeviceName = step.targetDeviceName,
                environmentType = step.environmentType.name,
                explanationPlainEnglish = step.explanationPlainEnglish,
                commandToRun = step.commandToRun,
                expectedResult = step.expectedResult,
                impactLevel = step.impactLevel.name,
                ifItFailsHint = step.ifItFailsHint,
                isCompleted = step.isCompleted
            )
        }
        workflowDao().upsertWorkflowSteps(stepEntities)
    }
}
