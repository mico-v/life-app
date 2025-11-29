package com.example.android16demo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * TaskTemplate Entity - represents a task template for quick task creation.
 * Templates allow users to quickly create common task types.
 */
@Entity(tableName = "task_templates")
data class TaskTemplate(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val defaultTitle: String,
    val defaultDescription: String? = null,
    val defaultDurationMinutes: Int? = null,  // Default task duration
    val defaultPriority: Int = Task.PRIORITY_MEDIUM,
    val iconName: String = "default",  // Icon identifier
    val colorHex: String = "#6650a4",  // Template color
    val sortOrder: Int = 0,  // For ordering templates
    val createdAt: Long = System.currentTimeMillis(),
    val isBuiltIn: Boolean = false  // System templates vs user-created
) {
    companion object {
        /**
         * Create default templates
         */
        fun getDefaultTemplates(): List<TaskTemplate> = listOf(
            TaskTemplate(
                id = "template_work",
                name = "Work",
                defaultTitle = "Work Task",
                defaultPriority = Task.PRIORITY_MEDIUM,
                iconName = "work",
                colorHex = "#1976D2",
                sortOrder = 0,
                isBuiltIn = true
            ),
            TaskTemplate(
                id = "template_study",
                name = "Study",
                defaultTitle = "Study Session",
                defaultDurationMinutes = 60,
                defaultPriority = Task.PRIORITY_MEDIUM,
                iconName = "school",
                colorHex = "#7B1FA2",
                sortOrder = 1,
                isBuiltIn = true
            ),
            TaskTemplate(
                id = "template_exercise",
                name = "Exercise",
                defaultTitle = "Workout",
                defaultDurationMinutes = 45,
                defaultPriority = Task.PRIORITY_LOW,
                iconName = "fitness",
                colorHex = "#388E3C",
                sortOrder = 2,
                isBuiltIn = true
            ),
            TaskTemplate(
                id = "template_meeting",
                name = "Meeting",
                defaultTitle = "Meeting",
                defaultDurationMinutes = 30,
                defaultPriority = Task.PRIORITY_HIGH,
                iconName = "meeting",
                colorHex = "#F57C00",
                sortOrder = 3,
                isBuiltIn = true
            ),
            TaskTemplate(
                id = "template_personal",
                name = "Personal",
                defaultTitle = "Personal Task",
                defaultPriority = Task.PRIORITY_LOW,
                iconName = "person",
                colorHex = "#C2185B",
                sortOrder = 4,
                isBuiltIn = true
            )
        )
    }
    
    /**
     * Create a Task from this template
     */
    fun toTask(customTitle: String? = null): Task {
        val now = System.currentTimeMillis()
        return Task(
            title = customTitle ?: defaultTitle,
            description = defaultDescription,
            startTime = now,
            deadline = defaultDurationMinutes?.let { now + it * 60 * 1000L },
            priority = defaultPriority
        )
    }
}
