package com.example.android16demo.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for TaskDetailViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TaskRepository
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `new task mode has correct initial state`() = runTest {
        // When
        val viewModel = TaskDetailViewModel(repository, null)
        
        // Then
        assertFalse(viewModel.isEditMode)
        assertEquals("", viewModel.uiState.value.title)
        assertEquals("", viewModel.uiState.value.description)
        assertEquals(Task.PRIORITY_MEDIUM, viewModel.uiState.value.priority)
        assertEquals(0f, viewModel.uiState.value.progress, 0.001f)
        assertFalse(viewModel.uiState.value.isPublic)
    }
    
    @Test
    fun `edit mode loads existing task`() = runTest {
        // Given
        val existingTask = Task(
            id = "task-123",
            title = "Existing Task",
            description = "Existing Description",
            priority = Task.PRIORITY_HIGH,
            progress = 0.5f,
            isPublic = true
        )
        whenever(repository.getTaskByIdOnce("task-123")).thenReturn(existingTask)
        
        // When
        val viewModel = TaskDetailViewModel(repository, "task-123")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(viewModel.isEditMode)
        assertEquals("Existing Task", viewModel.uiState.value.title)
        assertEquals("Existing Description", viewModel.uiState.value.description)
        assertEquals(Task.PRIORITY_HIGH, viewModel.uiState.value.priority)
        assertEquals(0.5f, viewModel.uiState.value.progress, 0.001f)
        assertTrue(viewModel.uiState.value.isPublic)
    }
    
    @Test
    fun `updateTitle updates state`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        
        // When
        viewModel.updateTitle("New Title")
        
        // Then
        assertEquals("New Title", viewModel.uiState.value.title)
        assertNull(viewModel.uiState.value.titleError)
    }
    
    @Test
    fun `updateTitle with blank shows error`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        
        // When
        viewModel.updateTitle("")
        
        // Then
        assertEquals("", viewModel.uiState.value.title)
        assertEquals("Title is required", viewModel.uiState.value.titleError)
    }
    
    @Test
    fun `updateDescription updates state`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        
        // When
        viewModel.updateDescription("New Description")
        
        // Then
        assertEquals("New Description", viewModel.uiState.value.description)
    }
    
    @Test
    fun `updatePriority updates state`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        
        // When
        viewModel.updatePriority(Task.PRIORITY_HIGH)
        
        // Then
        assertEquals(Task.PRIORITY_HIGH, viewModel.uiState.value.priority)
    }
    
    @Test
    fun `updateProgress clamps to valid range`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        
        // When - too low
        viewModel.updateProgress(-0.5f)
        assertEquals(0f, viewModel.uiState.value.progress, 0.001f)
        
        // When - too high
        viewModel.updateProgress(1.5f)
        assertEquals(1f, viewModel.uiState.value.progress, 0.001f)
        
        // When - valid
        viewModel.updateProgress(0.5f)
        assertEquals(0.5f, viewModel.uiState.value.progress, 0.001f)
    }
    
    @Test
    fun `updateDeadline updates state`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        val deadline = System.currentTimeMillis() + 3600000
        
        // When
        viewModel.updateDeadline(deadline)
        
        // Then
        assertEquals(deadline, viewModel.uiState.value.deadline)
    }
    
    @Test
    fun `updateStartTime updates state`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        val startTime = System.currentTimeMillis()
        
        // When
        viewModel.updateStartTime(startTime)
        
        // Then
        assertEquals(startTime, viewModel.uiState.value.startTime)
    }
    
    @Test
    fun `updateIsPublic updates state`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        
        // When
        viewModel.updateIsPublic(true)
        
        // Then
        assertTrue(viewModel.uiState.value.isPublic)
    }
    
    @Test
    fun `saveTask with valid data creates new task`() = runTest {
        // Given
        whenever(repository.pushTask(any())).thenReturn(Unit)
        val viewModel = TaskDetailViewModel(repository, null)
        viewModel.updateTitle("New Task")
        viewModel.updateDescription("Description")
        
        // When
        viewModel.saveTask()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(repository).pushTask(any())
        assertTrue(viewModel.uiState.value.isSaved)
    }
    
    @Test
    fun `saveTask with blank title shows error and does not save`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        viewModel.updateTitle("")
        
        // When
        viewModel.saveTask()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(repository, never()).pushTask(any())
        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals("Title is required", viewModel.uiState.value.titleError)
    }
    
    @Test
    fun `saveTask in edit mode updates existing task`() = runTest {
        // Given
        val existingTask = Task(id = "task-123", title = "Old Title")
        whenever(repository.getTaskByIdOnce("task-123")).thenReturn(existingTask)
        whenever(repository.updateTask(any())).thenReturn(Unit)
        
        val viewModel = TaskDetailViewModel(repository, "task-123")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateTitle("Updated Title")
        
        // When
        viewModel.saveTask()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(repository).updateTask(any())
        assertTrue(viewModel.uiState.value.isSaved)
    }
    
    @Test
    fun `clearError clears error message`() = runTest {
        // Given
        val viewModel = TaskDetailViewModel(repository, null)
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
