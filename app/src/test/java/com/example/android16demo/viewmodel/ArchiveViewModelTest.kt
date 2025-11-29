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
 * Unit tests for ArchiveViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: ArchiveViewModel
    
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
        whenever(repository.getArchivedTasks()).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel = ArchiveViewModel(repository)
        
        // Then - before collecting the flow
        assertEquals(true, viewModel.uiState.value.isLoading)
    }
    
    @Test
    fun `loading archived tasks updates state with tasks`() = runTest {
        // Given
        val archivedTasks = listOf(
            Task(title = "Completed 1", isCompleted = true, completedAt = 1000L),
            Task(title = "Completed 2", isCompleted = true, completedAt = 2000L)
        )
        whenever(repository.getArchivedTasks()).thenReturn(flowOf(archivedTasks))
        
        // When
        viewModel = ArchiveViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.archivedTasks.size)
        assertEquals("Completed 1", viewModel.uiState.value.archivedTasks[0].title)
    }
    
    @Test
    fun `deleteTask calls repository deleteTaskById`() = runTest {
        // Given
        whenever(repository.getArchivedTasks()).thenReturn(flowOf(emptyList()))
        whenever(repository.deleteTaskById(any())).thenReturn(Unit)
        viewModel = ArchiveViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.deleteTask("task-123")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(repository).deleteTaskById("task-123")
    }
    
    @Test
    fun `clearAllArchived calls repository clearArchivedTasks`() = runTest {
        // Given
        whenever(repository.getArchivedTasks()).thenReturn(flowOf(emptyList()))
        whenever(repository.clearArchivedTasks()).thenReturn(Unit)
        viewModel = ArchiveViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearAllArchived()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(repository).clearArchivedTasks()
    }
    
    @Test
    fun `clearError clears error message`() = runTest {
        // Given
        whenever(repository.getArchivedTasks()).thenReturn(flowOf(emptyList()))
        viewModel = ArchiveViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
