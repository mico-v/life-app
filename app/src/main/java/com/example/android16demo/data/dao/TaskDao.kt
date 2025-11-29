package com.example.android16demo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.android16demo.data.entity.Task
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Task entity.
 * Provides all database operations for tasks.
 */
@Dao
interface TaskDao {
    
    /**
     * Get all tasks ordered by creation time (newest first)
     */
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Get all active (incomplete) tasks ordered by deadline (earliest first), then by creation time
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY CASE WHEN deadline IS NULL THEN 1 ELSE 0 END, deadline ASC, createdAt DESC")
    fun getActiveTasks(): Flow<List<Task>>
    
    /**
     * Get all archived (completed) tasks ordered by completion time (newest first)
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getArchivedTasks(): Flow<List<Task>>
    
    /**
     * Get a specific task by ID
     */
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: String): Flow<Task?>
    
    /**
     * Get a specific task by ID (suspend function for one-shot operations)
     */
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskByIdOnce(id: String): Task?
    
    /**
     * Get tasks within a specific time range (for timeline view)
     */
    @Query("SELECT * FROM tasks WHERE ((startTime BETWEEN :start AND :end) OR (deadline BETWEEN :start AND :end)) AND isCompleted = 0 ORDER BY COALESCE(startTime, deadline) ASC")
    fun getTasksInTimeRange(start: Long, end: Long): Flow<List<Task>>
    
    /**
     * Get tasks for today's view
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND (date(startTime/1000, 'unixepoch') = date(:todayMillis/1000, 'unixepoch') OR date(deadline/1000, 'unixepoch') = date(:todayMillis/1000, 'unixepoch')) ORDER BY COALESCE(startTime, deadline) ASC")
    fun getTodayTasks(todayMillis: Long): Flow<List<Task>>
    
    /**
     * Get overdue tasks
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND deadline < :currentTime ORDER BY deadline ASC")
    fun getOverdueTasks(currentTime: Long): Flow<List<Task>>
    
    /**
     * Get public tasks for sync (Phase 3)
     */
    @Query("SELECT * FROM tasks WHERE isPublic = 1 ORDER BY createdAt DESC")
    fun getPublicTasks(): Flow<List<Task>>
    
    /**
     * Get task count statistics
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND completedAt >= :since")
    suspend fun getCompletedTaskCountSince(since: Long): Int
    
    /**
     * Get total focus time (sum of time spent on completed tasks)
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    suspend fun getActiveTaskCount(): Int
    
    /**
     * Insert a new task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)
    
    /**
     * Insert multiple tasks
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)
    
    /**
     * Update an existing task
     */
    @Update
    suspend fun updateTask(task: Task)
    
    /**
     * Delete a task
     */
    @Delete
    suspend fun deleteTask(task: Task)
    
    /**
     * Delete a task by ID
     */
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
    
    /**
     * Delete all completed tasks
     */
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteAllArchivedTasks()
    
    /**
     * Mark a task as completed (Pop)
     */
    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :completedAt, progress = 1.0 WHERE id = :id")
    suspend fun completeTask(id: String, completedAt: Long = System.currentTimeMillis())
    
    /**
     * Update task progress
     */
    @Query("UPDATE tasks SET progress = :progress WHERE id = :id")
    suspend fun updateTaskProgress(id: String, progress: Float)
}
