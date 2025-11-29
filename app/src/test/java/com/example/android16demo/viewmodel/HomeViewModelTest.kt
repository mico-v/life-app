package com.example.android16demo.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for HomeViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: HomeViewModel
    
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
    fun `initial state shows loading`() = runTest {
        // Given
        whenever(repository.getActiveTasks()).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel = HomeViewModel(repository)
        
        // Then - before collecting the flow
        assertEquals(true, viewModel.uiState.value.isLoading)
    }
    
    @Test
    fun `loading tasks updates state with tasks`() = runTest {
        // Given
        val tasks = listOf(
            Task(title = "Task 1"),
            Task(title = "Task 2")
        )
        whenever(repository.getActiveTasks()).thenReturn(flowOf(tasks))
        
        // When
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.activeTasks.size)
        assertEquals("Task 1", viewModel.uiState.value.activeTasks[0].title)
    }
    
    @Test
    fun `popTask calls repository popTask`() = runTest {
        // Given
        whenever(repository.getActiveTasks()).thenReturn(flowOf(emptyList()))
        whenever(repository.popTask(any())).thenReturn(Unit)
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.popTask("task-123")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(repository).popTask("task-123")
    }
    
    @Test
    fun `deleteTask calls repository deleteTaskById`() = runTest {
        // Given
        whenever(repository.getActiveTasks()).thenReturn(flowOf(emptyList()))
        whenever(repository.deleteTaskById(any())).thenReturn(Unit)
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.deleteTask("task-123")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(repository).deleteTaskById("task-123")
    }
    
    @Test
    fun `updateTaskProgress calls repository updateTaskProgress`() = runTest {
        // Given
        whenever(repository.getActiveTasks()).thenReturn(flowOf(emptyList()))
        whenever(repository.updateTaskProgress(any(), any())).thenReturn(Unit)
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updateTaskProgress("task-123", 0.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(repository).updateTaskProgress("task-123", 0.5f)
    }
    
    @Test
    fun `clearError clears error message`() = runTest {
        // Given
        whenever(repository.getActiveTasks()).thenReturn(flowOf(emptyList()))
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
