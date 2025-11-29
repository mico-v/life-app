package com.example.android16demo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.android16demo.data.entity.TaskTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TaskTemplate entity.
 */
@Dao
interface TaskTemplateDao {
    
    /**
     * Get all templates ordered by sort order
     */
    @Query("SELECT * FROM task_templates ORDER BY sortOrder ASC")
    fun getAllTemplates(): Flow<List<TaskTemplate>>
    
    /**
     * Get a specific template by ID
     */
    @Query("SELECT * FROM task_templates WHERE id = :id")
    suspend fun getTemplateById(id: String): TaskTemplate?
    
    /**
     * Get built-in templates
     */
    @Query("SELECT * FROM task_templates WHERE isBuiltIn = 1 ORDER BY sortOrder ASC")
    fun getBuiltInTemplates(): Flow<List<TaskTemplate>>
    
    /**
     * Get user-created templates
     */
    @Query("SELECT * FROM task_templates WHERE isBuiltIn = 0 ORDER BY sortOrder ASC")
    fun getUserTemplates(): Flow<List<TaskTemplate>>
    
    /**
     * Insert a template
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TaskTemplate)
    
    /**
     * Insert multiple templates
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<TaskTemplate>)
    
    /**
     * Update a template
     */
    @Update
    suspend fun updateTemplate(template: TaskTemplate)
    
    /**
     * Delete a template
     */
    @Delete
    suspend fun deleteTemplate(template: TaskTemplate)
    
    /**
     * Delete a template by ID
     */
    @Query("DELETE FROM task_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: String)
    
    /**
     * Check if built-in templates exist
     */
    @Query("SELECT COUNT(*) FROM task_templates WHERE isBuiltIn = 1")
    suspend fun getBuiltInTemplateCount(): Int
}
