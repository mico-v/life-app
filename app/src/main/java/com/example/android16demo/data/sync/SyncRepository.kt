package com.example.android16demo.data.sync

import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.repository.TaskRepository
import com.example.android16demo.network.RetrofitClient
import com.example.android16demo.network.model.SyncRequest
import com.example.android16demo.network.model.TaskDto
import kotlinx.coroutines.flow.first

/**
 * Repository for handling data synchronization with the server
 */
class SyncRepository(
    private val taskRepository: TaskRepository,
    private val syncPreferences: SyncPreferences
) {

    /**
     * Sync result sealed class
     */
    sealed class SyncResult {
        data class Success(val syncedCount: Int, val serverTime: Long) : SyncResult()
        data class Error(val message: String) : SyncResult()
        data object NotConfigured : SyncResult()
    }

    /**
     * Perform full sync with server
     */
    suspend fun syncTasks(): SyncResult {
        if (!syncPreferences.isSyncConfigured) {
            return SyncResult.NotConfigured
        }

        val clientToken = syncPreferences.clientToken
        val serverPassword = syncPreferences.serverPassword
        val api = RetrofitClient.getApi(syncPreferences.serverUrl)

        return try {
            val localTasks = taskRepository.getAllTasks().first()
            val taskDtos = localTasks.map { it.toDto() }

            val request = SyncRequest(
                userId = syncPreferences.userId ?: clientToken,
                tasks = taskDtos,
                lastSync = syncPreferences.lastSyncTime.takeIf { it > 0 }
            )

            val response = api.syncTasks(clientToken, serverPassword, request)

            if (response.isSuccessful) {
                val syncResponse = response.body()
                if (syncResponse?.success == true) {
                    syncResponse.updatedTasks?.forEach { dto ->
                        taskRepository.updateTask(dto.toEntity())
                    }
                    syncPreferences.lastSyncTime = syncResponse.serverTime
                    SyncResult.Success(syncedCount = taskDtos.size, serverTime = syncResponse.serverTime)
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
        if (!syncPreferences.isSyncConfigured) {
            return SyncResult.NotConfigured
        }

        val clientToken = syncPreferences.clientToken
        val serverPassword = syncPreferences.serverPassword
        val api = RetrofitClient.getApi(syncPreferences.serverUrl)

        return try {
            val publicTasks = taskRepository.getPublicTasks().first()
            val taskDtos = publicTasks.map { it.toDto() }

            val request = SyncRequest(
                userId = syncPreferences.userId ?: clientToken,
                tasks = taskDtos,
                lastSync = syncPreferences.lastSyncTime.takeIf { it > 0 }
            )

            val response = api.syncTasks(clientToken, serverPassword, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val syncResponse = response.body()!!
                syncPreferences.lastSyncTime = syncResponse.serverTime
                SyncResult.Success(syncedCount = taskDtos.size, serverTime = syncResponse.serverTime)
            } else {
                SyncResult.Error(response.body()?.message ?: "Sync failed")
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error during sync")
        }
    }

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
        isPublic = isPublic,
        tags = tags
    )

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
        isPublic = isPublic,
        tags = tags
    )
}
