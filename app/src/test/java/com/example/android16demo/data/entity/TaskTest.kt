package com.example.android16demo.data.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Task entity
 */
class TaskTest {
    
    @Test
    fun `task with default values should have correct defaults`() {
        val task = Task(title = "Test Task")
        
        assertEquals("Test Task", task.title)
        assertEquals(null, task.description)
        assertEquals(false, task.isCompleted)
        assertEquals(null, task.completedAt)
        assertEquals(0f, task.progress, 0.001f)
        assertEquals(Task.PRIORITY_LOW, task.priority)
        assertEquals(false, task.isPublic)
    }
    
    @Test
    fun `isOverdue returns true when deadline has passed and task is not completed`() {
        val pastDeadline = System.currentTimeMillis() - 3600000 // 1 hour ago
        val task = Task(
            title = "Overdue Task",
            deadline = pastDeadline,
            isCompleted = false
        )
        
        assertTrue(task.isOverdue())
    }
    
    @Test
    fun `isOverdue returns false when deadline has not passed`() {
        val futureDeadline = System.currentTimeMillis() + 3600000 // 1 hour from now
        val task = Task(
            title = "Future Task",
            deadline = futureDeadline,
            isCompleted = false
        )
        
        assertFalse(task.isOverdue())
    }
    
    @Test
    fun `isOverdue returns false when task is completed even if deadline passed`() {
        val pastDeadline = System.currentTimeMillis() - 3600000
        val task = Task(
            title = "Completed Task",
            deadline = pastDeadline,
            isCompleted = true
        )
        
        assertFalse(task.isOverdue())
    }
    
    @Test
    fun `isOverdue returns false when deadline is null`() {
        val task = Task(
            title = "No Deadline Task",
            deadline = null,
            isCompleted = false
        )
        
        assertFalse(task.isOverdue())
    }
    
    @Test
    fun `getPriorityLabel returns correct labels`() {
        val lowPriorityTask = Task(title = "Low", priority = Task.PRIORITY_LOW)
        val mediumPriorityTask = Task(title = "Medium", priority = Task.PRIORITY_MEDIUM)
        val highPriorityTask = Task(title = "High", priority = Task.PRIORITY_HIGH)
        
        assertEquals("Low", lowPriorityTask.getPriorityLabel())
        assertEquals("Medium", mediumPriorityTask.getPriorityLabel())
        assertEquals("High", highPriorityTask.getPriorityLabel())
    }
    
    @Test
    fun `getPriorityLabel returns Unknown for invalid priority`() {
        val invalidPriorityTask = Task(title = "Invalid", priority = 99)
        
        assertEquals("Unknown", invalidPriorityTask.getPriorityLabel())
    }
    
    @Test
    fun `priority constants have correct values`() {
        assertEquals(1, Task.PRIORITY_LOW)
        assertEquals(2, Task.PRIORITY_MEDIUM)
        assertEquals(3, Task.PRIORITY_HIGH)
    }
    
    @Test
    fun `task id is generated as UUID by default`() {
        val task = Task(title = "Test")
        
        // UUID format: 8-4-4-4-12 hex characters
        assertTrue(task.id.matches(Regex("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")))
    }
    
    @Test
    fun `task with custom id preserves the id`() {
        val customId = "custom-id-123"
        val task = Task(id = customId, title = "Custom ID Task")
        
        assertEquals(customId, task.id)
    }
    
    @Test
    fun `createdAt is set to current time by default`() {
        val before = System.currentTimeMillis()
        val task = Task(title = "Timed Task")
        val after = System.currentTimeMillis()
        
        assertTrue(task.createdAt >= before)
        assertTrue(task.createdAt <= after)
    }
}
