package com.zakshub.nexus.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.zakshub.nexus.data.local.entity.DeviceEntity
import com.zakshub.nexus.data.local.entity.EnvironmentEntity
import com.zakshub.nexus.data.local.entity.RecipeEntity
import com.zakshub.nexus.data.local.entity.TroubleshootingCaseEntity
import com.zakshub.nexus.data.local.entity.WorkflowEntity
import com.zakshub.nexus.data.local.entity.WorkflowStepEntity
import kotlinx.coroutines.flow.Flow

data class WorkflowWithSteps(
    @Embedded val workflow: WorkflowEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workflowId"
    )
    val steps: List<WorkflowStepEntity>
)

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY name ASC")
    fun getAllDevicesFlow(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY name ASC")
    suspend fun getAllDevices(): List<DeviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevices(devices: List<DeviceEntity>)
}

@Dao
interface EnvironmentDao {
    @Query("SELECT * FROM environments ORDER BY deviceName ASC, type ASC")
    fun getAllEnvironmentsFlow(): Flow<List<EnvironmentEntity>>

    @Query("SELECT * FROM environments ORDER BY deviceName ASC, type ASC")
    suspend fun getAllEnvironments(): List<EnvironmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEnvironments(environments: List<EnvironmentEntity>)
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY isPinnedDaily DESC, updatedAtEpochMs DESC")
    fun getAllRecipesFlow(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes ORDER BY isPinnedDaily DESC, updatedAtEpochMs DESC")
    suspend fun getAllRecipes(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE environmentType = :envType ORDER BY isPinnedDaily DESC, updatedAtEpochMs DESC")
    fun getRecipesByEnvironmentFlow(envType: String): Flow<List<RecipeEntity>>

    @Query("""
        SELECT * FROM recipes 
        WHERE title LIKE '%' || :query || '%' 
           OR purposePlainEnglish LIKE '%' || :query || '%' 
           OR commandSyntax LIKE '%' || :query || '%' 
           OR expectedOutputPlainEnglish LIKE '%' || :query || '%'
        ORDER BY isPinnedDaily DESC, updatedAtEpochMs DESC
    """)
    fun searchRecipesFlow(query: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: String): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipes(recipes: List<RecipeEntity>)

    @Query("UPDATE recipes SET verificationStatus = :newStatus, updatedAtEpochMs = :timestamp WHERE id = :id")
    suspend fun updateVerificationStatus(id: String, newStatus: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: String)
}

@Dao
interface TroubleshootingDao {
    @Query("SELECT * FROM troubleshooting_cases ORDER BY symptomTitle ASC")
    fun getAllCasesFlow(): Flow<List<TroubleshootingCaseEntity>>

    @Query("SELECT * FROM troubleshooting_cases ORDER BY symptomTitle ASC")
    suspend fun getAllCases(): List<TroubleshootingCaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCases(cases: List<TroubleshootingCaseEntity>)
}

@Dao
interface WorkflowDao {
    @Transaction
    @Query("SELECT * FROM workflows ORDER BY title ASC")
    fun getAllWorkflowsWithStepsFlow(): Flow<List<WorkflowWithSteps>>

    @Transaction
    @Query("SELECT * FROM workflows WHERE id = :workflowId LIMIT 1")
    fun getWorkflowWithStepsFlow(workflowId: String): Flow<WorkflowWithSteps?>

    @Transaction
    @Query("SELECT * FROM workflows ORDER BY title ASC")
    suspend fun getAllWorkflowsWithSteps(): List<WorkflowWithSteps>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkflows(workflows: List<WorkflowEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkflowSteps(steps: List<WorkflowStepEntity>)

    @Query("UPDATE workflow_steps SET isCompleted = :isCompleted WHERE id = :stepId")
    suspend fun updateStepCompletion(stepId: String, isCompleted: Boolean)

    @Query("UPDATE workflows SET activeStepNumber = :stepNumber WHERE id = :workflowId")
    suspend fun updateActiveStepNumber(workflowId: String, stepNumber: Int)
}
