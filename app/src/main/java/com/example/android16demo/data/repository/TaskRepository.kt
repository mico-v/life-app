package com.example.android16demo.data.repository

import com.example.android16demo.data.dao.TaskDao
import com.example.android16demo.data.entity.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Task operations.
 * Provides a clean API for the ViewModel to interact with data sources.
 */
class TaskRepository(private val taskDao: TaskDao) {
    
    /**
     * Get all tasks as a Flow for reactive updates
     */
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    
    /**
     * Get all active (incomplete) tasks
     */
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    
    /**
     * Get all archived (completed) tasks
     */
    fun getArchivedTasks(): Flow<List<Task>> = taskDao.getArchivedTasks()
    
    /**
     * Get a specific task by ID
     */
    fun getTaskById(id: String): Flow<Task?> = taskDao.getTaskById(id)
    
    /**
     * Get a specific task by ID (one-shot)
     */
    suspend fun getTaskByIdOnce(id: String): Task? = taskDao.getTaskByIdOnce(id)
    
    /**
     * Get tasks within a time range (for timeline view)
     */
    fun getTasksInTimeRange(start: Long, end: Long): Flow<List<Task>> =
        taskDao.getTasksInTimeRange(start, end)
    
    /**
     * Get tasks for today
     */
    fun getTodayTasks(): Flow<List<Task>> = taskDao.getTodayTasks(System.currentTimeMillis())
    
    /**
     * Get overdue tasks
     */
    fun getOverdueTasks(): Flow<List<Task>> = taskDao.getOverdueTasks(System.currentTimeMillis())
    
    /**
     * Get public tasks for sync
     */
    fun getPublicTasks(): Flow<List<Task>> = taskDao.getPublicTasks()
    
    /**
     * Push a new task (create)
     */
    suspend fun pushTask(task: Task) = taskDao.insertTask(task)
    
    /**
     * Push multiple tasks
     */
    suspend fun pushTasks(tasks: List<Task>) = taskDao.insertTasks(tasks)
    
    /**
     * Update an existing task
     */
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    
    /**
     * Pop a task (mark as completed)
     */
    suspend fun popTask(id: String) = taskDao.completeTask(id, System.currentTimeMillis())
    
    /**
     * Delete a task
     */
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    
    /**
     * Delete a task by ID
     */
    suspend fun deleteTaskById(id: String) = taskDao.deleteTaskById(id)
    
    /**
     * Clear all archived tasks
     */
    suspend fun clearArchivedTasks() = taskDao.deleteAllArchivedTasks()
    
    /**
     * Update task progress
     */
    suspend fun updateTaskProgress(id: String, progress: Float) = 
        taskDao.updateTaskProgress(id, progress.coerceIn(0f, 1f))
    
    /**
     * Get completed task count since a specific time
     */
    suspend fun getCompletedTaskCountSince(since: Long): Int =
        taskDao.getCompletedTaskCountSince(since)
    
    /**
     * Get active task count
     */
    suspend fun getActiveTaskCount(): Int = taskDao.getActiveTaskCount()
    
    /**
     * Create a new task with validation
     */
    suspend fun createTask(
        title: String,
        description: String? = null,
        startTime: Long? = null,
        deadline: Long? = null,
        priority: Int = Task.PRIORITY_MEDIUM,
        isPublic: Boolean = false
    ): Result<Task> {
        return try {
            if (title.isBlank()) {
                return Result.failure(IllegalArgumentException("Title cannot be empty"))
            }
            
            val task = Task(
                title = title.trim(),
                description = description?.trim(),
                startTime = startTime,
                deadline = deadline,
                priority = priority.coerceIn(Task.PRIORITY_LOW, Task.PRIORITY_HIGH),
                isPublic = isPublic
            )
            
            pushTask(task)
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
