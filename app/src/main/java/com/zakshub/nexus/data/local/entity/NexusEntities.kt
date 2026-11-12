package com.zakshub.nexus.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val platform: String,
    val hardwareSummary: String,
    val roleDescription: String,
    val isVerified: Boolean
)

@Entity(tableName = "environments")
data class EnvironmentEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val deviceName: String,
    val type: String,
    val shellSummary: String,
    val filesystemBoundaryNote: String,
    val commonPitfallWarning: String
)

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
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
    val rollbackOrUndoHint: String?,
    val isPinnedDaily: Boolean,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)


@Entity(tableName = "troubleshooting_cases")
data class TroubleshootingCaseEntity(
    @PrimaryKey val id: String,
    val symptomTitle: String,
    val exactErrorSnippet: String,
    val targetDeviceName: String,
    val environmentType: String,
    val plainEnglishWhyItHappens: String,
    val safeReadOnlyCheckCommand: String,
    val verifiedFixCommand: String,
    val verificationStatus: String
)

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summaryPlainEnglish: String,
    val activeStepNumber: Int,
    val isCompleted: Boolean
)

@Entity(tableName = "workflow_steps")
data class WorkflowStepEntity(
    @PrimaryKey val id: String,
    val workflowId: String,
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
