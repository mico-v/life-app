package com.example.android16demo.data.repository

import com.example.android16demo.data.dao.TaskDao
import com.example.android16demo.data.entity.Task
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for TaskRepository
 */
class TaskRepositoryTest {
    
    private lateinit var taskDao: TaskDao
    private lateinit var repository: TaskRepository
    
    @Before
    fun setup() {
        taskDao = mock()
        repository = TaskRepository(taskDao)
    }
    
    @Test
    fun `createTask with valid title creates task`() = runTest {
        // Given
        whenever(taskDao.insertTask(any())).thenReturn(Unit)
        
        // When
        val result = repository.createTask(
            title = "New Task",
            description = "Description",
            priority = Task.PRIORITY_HIGH
        )
        
        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { task ->
            assertEquals("New Task", task.title)
            assertEquals("Description", task.description)
            assertEquals(Task.PRIORITY_HIGH, task.priority)
        }
    }
    
    @Test
    fun `createTask with blank title returns failure`() = runTest {
        // When
        val result = repository.createTask(title = "   ")
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `createTask with empty title returns failure`() = runTest {
        // When
        val result = repository.createTask(title = "")
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `createTask trims title and description`() = runTest {
        // Given
        whenever(taskDao.insertTask(any())).thenReturn(Unit)
        
        // When
        val result = repository.createTask(
            title = "  Trimmed Title  ",
            description = "  Trimmed Description  "
        )
        
        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { task ->
            assertEquals("Trimmed Title", task.title)
            assertEquals("Trimmed Description", task.description)
        }
    }
    
    @Test
    fun `createTask with blank description sets null`() = runTest {
        // Given
        whenever(taskDao.insertTask(any())).thenReturn(Unit)
        
        // When
        val result = repository.createTask(
            title = "Task",
            description = "   "
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull()?.description)
    }
    
    @Test
    fun `createTask clamps priority to valid range`() = runTest {
        // Given
        whenever(taskDao.insertTask(any())).thenReturn(Unit)
        
        // When - priority too low
        val resultLow = repository.createTask(title = "Task", priority = 0)
        
        // Then
        assertTrue(resultLow.isSuccess)
        assertEquals(Task.PRIORITY_LOW, resultLow.getOrNull()?.priority)
        
        // When - priority too high
        val resultHigh = repository.createTask(title = "Task", priority = 10)
        
        // Then
        assertTrue(resultHigh.isSuccess)
        assertEquals(Task.PRIORITY_HIGH, resultHigh.getOrNull()?.priority)
    }
    
    @Test
    fun `updateTaskProgress clamps progress to 0-1 range`() = runTest {
        // Given
        whenever(taskDao.updateTaskProgress(any(), any())).thenReturn(Unit)
        
        // When - progress too low
        repository.updateTaskProgress("task-id", -0.5f)
        
        // Then
        verify(taskDao).updateTaskProgress("task-id", 0f)
        
        // When - progress too high
        repository.updateTaskProgress("task-id", 1.5f)
        
        // Then
        verify(taskDao).updateTaskProgress("task-id", 1f)
    }
    
    @Test
    fun `getActiveTasks returns flow from dao`() = runTest {
        // Given
        val tasks = listOf(
            Task(title = "Task 1"),
            Task(title = "Task 2")
        )
        whenever(taskDao.getActiveTasks()).thenReturn(flowOf(tasks))
        
        // When
        val flow = repository.getActiveTasks()
        
        // Then
        flow.collect { result ->
            assertEquals(2, result.size)
            assertEquals("Task 1", result[0].title)
            assertEquals("Task 2", result[1].title)
        }
    }
    
    @Test
    fun `popTask calls completeTask on dao`() = runTest {
        // Given
        whenever(taskDao.completeTask(any(), any())).thenReturn(Unit)
        
        // When
        repository.popTask("task-id")
        
        // Then
        verify(taskDao).completeTask("task-id")
    }
    
    @Test
    fun `deleteTaskById calls deleteTaskById on dao`() = runTest {
        // Given
        whenever(taskDao.deleteTaskById(any())).thenReturn(Unit)
        
        // When
        repository.deleteTaskById("task-id")
        
        // Then
        verify(taskDao).deleteTaskById("task-id")
    }
    
    @Test
    fun `clearArchivedTasks calls deleteAllArchivedTasks on dao`() = runTest {
        // Given
        whenever(taskDao.deleteAllArchivedTasks()).thenReturn(Unit)
        
        // When
        repository.clearArchivedTasks()
        
        // Then
        verify(taskDao).deleteAllArchivedTasks()
    }
}
