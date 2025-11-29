package com.example.android16demo.data.repository

import com.example.android16demo.data.dao.TaskTemplateDao
import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.entity.TaskTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Repository for TaskTemplate operations
 */
class TemplateRepository(private val templateDao: TaskTemplateDao) {
    
    /**
     * Get all templates
     */
    fun getAllTemplates(): Flow<List<TaskTemplate>> = templateDao.getAllTemplates()
    
    /**
     * Get built-in templates
     */
    fun getBuiltInTemplates(): Flow<List<TaskTemplate>> = templateDao.getBuiltInTemplates()
    
    /**
     * Get user-created templates
     */
    fun getUserTemplates(): Flow<List<TaskTemplate>> = templateDao.getUserTemplates()
    
    /**
     * Get a specific template by ID
     */
    suspend fun getTemplateById(id: String): TaskTemplate? = templateDao.getTemplateById(id)
    
    /**
     * Create a new template
     */
    suspend fun createTemplate(template: TaskTemplate) = templateDao.insertTemplate(template)
    
    /**
     * Update an existing template
     */
    suspend fun updateTemplate(template: TaskTemplate) = templateDao.updateTemplate(template)
    
    /**
     * Delete a template
     */
    suspend fun deleteTemplate(template: TaskTemplate) = templateDao.deleteTemplate(template)
    
    /**
     * Delete a template by ID
     */
    suspend fun deleteTemplateById(id: String) = templateDao.deleteTemplateById(id)
    
    /**
     * Create a task from a template
     */
    suspend fun createTaskFromTemplate(templateId: String, customTitle: String? = null): Task? {
        val template = getTemplateById(templateId)
        return template?.toTask(customTitle)
    }
    
    /**
     * Initialize default templates if they don't exist
     */
    suspend fun initializeDefaultTemplates() {
        val existingCount = templateDao.getBuiltInTemplateCount()
        if (existingCount == 0) {
            templateDao.insertTemplates(TaskTemplate.getDefaultTemplates())
        }
    }
}
