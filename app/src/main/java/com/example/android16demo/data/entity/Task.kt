package com.example.android16demo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Task Entity - represents a task in the Life App stack/queue system.
 * Following the "Push to Start, Pop to Finish" concept.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startTime: Long? = null,     // Planned start time
    val deadline: Long? = null,       // Deadline (DDL)
    val isCompleted: Boolean = false, // Whether the task has been "Popped"
    val completedAt: Long? = null,    // When the task was completed
    val progress: Float = 0f,         // 0.0 - 1.0
    val priority: Int = 1,            // 1: Low, 2: Medium, 3: High
    val isPublic: Boolean = false,    // For server sync (Phase 3)
    val tags: String? = null          // Comma-separated tags
) {
    companion object {
        const val PRIORITY_LOW = 1
        const val PRIORITY_MEDIUM = 2
        const val PRIORITY_HIGH = 3
    }
    
    /**
     * Check if the task is overdue based on deadline
     */
    fun isOverdue(): Boolean {
        return deadline != null && !isCompleted && System.currentTimeMillis() > deadline
    }
    
    /**
     * Get the priority label
     */
    fun getPriorityLabel(): String {
        return when (priority) {
            PRIORITY_LOW -> "Low"
            PRIORITY_MEDIUM -> "Medium"
            PRIORITY_HIGH -> "High"
            else -> "Unknown"
        }
    }
    
    /**
     * Get list of tags
     */
    fun getTagList(): List<String> {
        return tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
    
    /**
     * Check if task has a specific tag
     */
    fun hasTag(tag: String): Boolean {
        return getTagList().any { it.equals(tag, ignoreCase = true) }
    }
}
