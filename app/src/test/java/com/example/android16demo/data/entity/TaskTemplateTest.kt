package com.example.android16demo.data.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for TaskTemplate entity
 */
class TaskTemplateTest {
    
    @Test
    fun `template with default values should have correct defaults`() {
        val template = TaskTemplate(
            name = "Test Template",
            defaultTitle = "Test Task"
        )
        
        assertEquals("Test Template", template.name)
        assertEquals("Test Task", template.defaultTitle)
        assertEquals(null, template.defaultDescription)
        assertEquals(null, template.defaultDurationMinutes)
        assertEquals(Task.PRIORITY_MEDIUM, template.defaultPriority)
        assertEquals("default", template.iconName)
        assertEquals("#6650a4", template.colorHex)
        assertEquals(0, template.sortOrder)
        assertFalse(template.isBuiltIn)
    }
    
    @Test
    fun `getDefaultTemplates returns expected templates`() {
        val templates = TaskTemplate.getDefaultTemplates()
        
        assertEquals(5, templates.size)
        
        val templateNames = templates.map { it.name }
        assertTrue(templateNames.contains("Work"))
        assertTrue(templateNames.contains("Study"))
        assertTrue(templateNames.contains("Exercise"))
        assertTrue(templateNames.contains("Meeting"))
        assertTrue(templateNames.contains("Personal"))
    }
    
    @Test
    fun `all default templates are marked as built-in`() {
        val templates = TaskTemplate.getDefaultTemplates()
        
        templates.forEach { template ->
            assertTrue("Template ${template.name} should be built-in", template.isBuiltIn)
        }
    }
    
    @Test
    fun `default templates have unique IDs`() {
        val templates = TaskTemplate.getDefaultTemplates()
        val ids = templates.map { it.id }
        
        assertEquals(ids.size, ids.distinct().size)
    }
    
    @Test
    fun `toTask creates task with template values`() {
        val template = TaskTemplate(
            name = "Work",
            defaultTitle = "Work Task",
            defaultDescription = "Work description",
            defaultDurationMinutes = 60,
            defaultPriority = Task.PRIORITY_HIGH
        )
        
        val task = template.toTask()
        
        assertEquals("Work Task", task.title)
        assertEquals("Work description", task.description)
        assertEquals(Task.PRIORITY_HIGH, task.priority)
        assertNotNull(task.startTime)
        assertNotNull(task.deadline)
    }
    
    @Test
    fun `toTask uses custom title when provided`() {
        val template = TaskTemplate(
            name = "Work",
            defaultTitle = "Default Title"
        )
        
        val task = template.toTask("Custom Title")
        
        assertEquals("Custom Title", task.title)
    }
    
    @Test
    fun `toTask calculates deadline from duration`() {
        val template = TaskTemplate(
            name = "Test",
            defaultTitle = "Test",
            defaultDurationMinutes = 30
        )
        
        val beforeCreate = System.currentTimeMillis()
        val task = template.toTask()
        val afterCreate = System.currentTimeMillis()
        
        // Deadline should be approximately 30 minutes after start
        val expectedMinDeadline = beforeCreate + 30 * 60 * 1000L
        val expectedMaxDeadline = afterCreate + 30 * 60 * 1000L
        
        assertNotNull(task.deadline)
        assertTrue(task.deadline!! >= expectedMinDeadline)
        assertTrue(task.deadline!! <= expectedMaxDeadline)
    }
    
    @Test
    fun `toTask sets null deadline when no duration`() {
        val template = TaskTemplate(
            name = "Test",
            defaultTitle = "Test",
            defaultDurationMinutes = null
        )
        
        val task = template.toTask()
        
        assertEquals(null, task.deadline)
    }
    
    @Test
    fun `template id is generated as UUID by default`() {
        val template = TaskTemplate(name = "Test", defaultTitle = "Test")
        
        // UUID format: 8-4-4-4-12 hex characters
        assertTrue(template.id.matches(Regex("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")))
    }
    
    @Test
    fun `default templates have correct sort order`() {
        val templates = TaskTemplate.getDefaultTemplates()
        
        for (i in 0 until templates.size - 1) {
            assertTrue(
                "Templates should be in ascending sort order",
                templates[i].sortOrder <= templates[i + 1].sortOrder
            )
        }
    }
}
