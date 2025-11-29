package com.example.android16demo.data.sync

import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.repository.TaskRepository
import com.example.android16demo.network.api.LifeAppApi
import com.example.android16demo.network.model.SyncRequest
import com.example.android16demo.network.model.TaskDto
import kotlinx.coroutines.flow.first

/**
 * Repository for handling data synchronization with the server
 */
class SyncRepository(
    private val api: LifeAppApi,
    private val taskRepository: TaskRepository,
    private val syncPreferences: SyncPreferences
) {
    
    /**
     * Sync result sealed class
     */
    sealed class SyncResult {
        data class Success(val syncedCount: Int, val serverTime: Long) : SyncResult()
        data class Error(val message: String) : SyncResult()
        data object NotLoggedIn : SyncResult()
    }
    
    /**
     * Perform full sync with server
     */
    suspend fun syncTasks(): SyncResult {
        if (!syncPreferences.isLoggedIn) {
            return SyncResult.NotLoggedIn
        }
        
        val token = syncPreferences.authToken ?: return SyncResult.NotLoggedIn
        val userId = syncPreferences.userId ?: return SyncResult.NotLoggedIn
        
        return try {
            // Get all tasks from local database
            val localTasks = taskRepository.getAllTasks().first()
            
            // Convert to DTOs
            val taskDtos = localTasks.map { it.toDto() }
            
            // Create sync request
            val request = SyncRequest(
                userId = userId,
                tasks = taskDtos,
                lastSync = syncPreferences.lastSyncTime.takeIf { it > 0 }
            )
            
            // Send to server
            val response = api.syncTasks("Bearer $token", request)
            
            if (response.isSuccessful) {
                val syncResponse = response.body()
                if (syncResponse?.success == true) {
                    // Update local database with any server changes
                    syncResponse.updatedTasks?.forEach { dto ->
                        val task = dto.toEntity()
                        taskRepository.updateTask(task)
                    }
                    
                    // Update last sync time
                    syncPreferences.lastSyncTime = syncResponse.serverTime
                    
                    SyncResult.Success(
                        syncedCount = taskDtos.size,
                        serverTime = syncResponse.serverTime
                    )
                } else {
                    SyncResult.Error(syncResponse?.message ?: "Sync failed")
                }
            } else {
                SyncResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error during sync")
        }
    }
    
    /**
     * Sync only public tasks (for status broadcast)
     */
    suspend fun syncPublicTasks(): SyncResult {
        if (!syncPreferences.isLoggedIn) {
            return SyncResult.NotLoggedIn
        }
        
        val token = syncPreferences.authToken ?: return SyncResult.NotLoggedIn
        val userId = syncPreferences.userId ?: return SyncResult.NotLoggedIn
        
        return try {
            // Get only public tasks
            val publicTasks = taskRepository.getPublicTasks().first()
            val taskDtos = publicTasks.map { it.toDto() }
            
            val request = SyncRequest(
                userId = userId,
                tasks = taskDtos,
                lastSync = syncPreferences.lastSyncTime.takeIf { it > 0 }
            )
            
            val response = api.syncTasks("Bearer $token", request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val syncResponse = response.body()!!
                syncPreferences.lastSyncTime = syncResponse.serverTime
                SyncResult.Success(
                    syncedCount = taskDtos.size,
                    serverTime = syncResponse.serverTime
                )
            } else {
                SyncResult.Error(response.body()?.message ?: "Sync failed")
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error during sync")
        }
    }
    
    /**
     * Login to server
     */
    suspend fun login(username: String, password: String): Result<String> {
        return try {
            val request = com.example.android16demo.network.model.AuthRequest(username, password)
            val response = api.login(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.token != null) {
                    // Store credentials
                    syncPreferences.authToken = authResponse.token
                    syncPreferences.userId = authResponse.userId
                    syncPreferences.username = authResponse.username
                    
                    Result.success(authResponse.token)
                } else {
                    Result.failure(Exception(authResponse?.error ?: "Login failed"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Register new account
     */
    suspend fun register(username: String, password: String): Result<String> {
        return try {
            val request = com.example.android16demo.network.model.AuthRequest(username, password)
            val response = api.register(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.token != null) {
                    syncPreferences.authToken = authResponse.token
                    syncPreferences.userId = authResponse.userId
                    syncPreferences.username = authResponse.username
                    
                    Result.success(authResponse.token)
                } else {
                    Result.failure(Exception(authResponse?.error ?: "Registration failed"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Logout
     */
    suspend fun logout(): Result<Unit> {
        return try {
            val token = syncPreferences.authToken
            if (token != null) {
                api.logout("Bearer $token")
            }
            syncPreferences.clearAuth()
            Result.success(Unit)
        } catch (e: Exception) {
            syncPreferences.clearAuth()
            Result.success(Unit) // Clear local auth even if server logout fails
        }
    }
    
    /**
     * Extension function to convert Task to TaskDto
     */
    private fun Task.toDto(): TaskDto = TaskDto(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        startTime = startTime,
        deadline = deadline,
        isCompleted = isCompleted,
        completedAt = completedAt,
        progress = progress,
        priority = priority,
        isPublic = isPublic
    )
    
    /**
     * Extension function to convert TaskDto to Task
     */
    private fun TaskDto.toEntity(): Task = Task(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        startTime = startTime,
        deadline = deadline,
        isCompleted = isCompleted,
        completedAt = completedAt,
        progress = progress,
        priority = priority,
        isPublic = isPublic
    )
}
