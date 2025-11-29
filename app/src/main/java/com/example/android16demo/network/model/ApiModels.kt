package com.example.android16demo.network.model

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects for API communication
 */

/**
 * Task DTO for syncing with server
 */
data class TaskDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("start_time") val startTime: Long?,
    @SerializedName("deadline") val deadline: Long?,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("completed_at") val completedAt: Long?,
    @SerializedName("progress") val progress: Float,
    @SerializedName("priority") val priority: Int,
    @SerializedName("is_public") val isPublic: Boolean
)

/**
 * Request body for sync endpoint
 */
data class SyncRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("tasks") val tasks: List<TaskDto>,
    @SerializedName("last_sync") val lastSync: Long?
)

/**
 * Response from sync endpoint
 */
data class SyncResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("server_time") val serverTime: Long,
    @SerializedName("updated_tasks") val updatedTasks: List<TaskDto>?
)

/**
 * User status response for public profile
 */
data class UserStatusResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("status") val status: String, // "BUSY" or "FREE"
    @SerializedName("current_task") val currentTask: String?,
    @SerializedName("public_tasks") val publicTasks: List<TaskDto>,
    @SerializedName("stats") val stats: UserStats?
)

/**
 * User statistics
 */
data class UserStats(
    @SerializedName("completed_today") val completedToday: Int,
    @SerializedName("completed_this_week") val completedThisWeek: Int,
    @SerializedName("active_tasks") val activeTasks: Int,
    @SerializedName("total_focus_hours") val totalFocusHours: Float?
)

/**
 * Authentication request
 */
data class AuthRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

/**
 * Authentication response
 */
data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String?,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("error") val error: String?
)
