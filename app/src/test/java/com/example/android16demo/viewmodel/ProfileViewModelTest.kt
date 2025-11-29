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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for ProfileViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: ProfileViewModel
    
    private val now = System.currentTimeMillis()
    
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
        whenever(repository.getAllTasks()).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel = ProfileViewModel(repository)
        
        // Then - before loading completes
        assertEquals(true, viewModel.uiState.value.isLoading)
    }
    
    @Test
    fun `loading completes and shows statistics`() = runTest {
        // Given
        val tasks = listOf(
            Task(title = "Active Task 1"),
            Task(title = "Active Task 2"),
            Task(title = "Completed Task", isCompleted = true, completedAt = now)
        )
        whenever(repository.getAllTasks()).thenReturn(flowOf(tasks))
        
        // When
        viewModel = ProfileViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.statistics.activeTasks)
        assertEquals(1, viewModel.uiState.value.statistics.totalCompletedAllTime)
    }
    
    @Test
    fun `statistics calculates completed today correctly`() = runTest {
        // Given - tasks completed today
        val startOfToday = now - (now % (24 * 60 * 60 * 1000))
        val tasks = listOf(
            Task(title = "Completed Today 1", isCompleted = true, completedAt = startOfToday + 1000),
            Task(title = "Completed Today 2", isCompleted = true, completedAt = startOfToday + 2000),
            Task(title = "Completed Yesterday", isCompleted = true, completedAt = startOfToday - 1000)
        )
        whenever(repository.getAllTasks()).thenReturn(flowOf(tasks))
        
        // When
        viewModel = ProfileViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(2, viewModel.uiState.value.statistics.completedToday)
    }
    
    @Test
    fun `statistics calculates active tasks correctly`() = runTest {
        // Given
        val tasks = listOf(
            Task(title = "Active 1", isCompleted = false),
            Task(title = "Active 2", isCompleted = false),
            Task(title = "Completed", isCompleted = true)
        )
        whenever(repository.getAllTasks()).thenReturn(flowOf(tasks))
        
        // When
        viewModel = ProfileViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(2, viewModel.uiState.value.statistics.activeTasks)
    }
    
    @Test
    fun `completion rate is 100 when all tasks completed on time`() = runTest {
        // Given - all tasks completed before deadline
        val tasks = listOf(
            Task(
                title = "On Time 1",
                isCompleted = true,
                deadline = now + 1000,
                completedAt = now
            ),
            Task(
                title = "On Time 2",
                isCompleted = true,
                deadline = now + 1000,
                completedAt = now
            )
        )
        whenever(repository.getAllTasks()).thenReturn(flowOf(tasks))
        
        // When
        viewModel = ProfileViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(100f, viewModel.uiState.value.statistics.completionRate, 0.01f)
    }
    
    @Test
    fun `completion rate is 50 when half tasks completed on time`() = runTest {
        // Given - one on time, one late
        val tasks = listOf(
            Task(
                title = "On Time",
                isCompleted = true,
                deadline = now + 1000,
                completedAt = now
            ),
            Task(
                title = "Late",
                isCompleted = true,
                deadline = now - 1000,
                completedAt = now
            )
        )
        whenever(repository.getAllTasks()).thenReturn(flowOf(tasks))
        
        // When
        viewModel = ProfileViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(50f, viewModel.uiState.value.statistics.completionRate, 0.01f)
    }
    
    @Test
    fun `completion rate is 0 when no tasks have deadline`() = runTest {
        // Given - tasks without deadlines
        val tasks = listOf(
            Task(title = "No Deadline", isCompleted = true, deadline = null)
        )
        whenever(repository.getAllTasks()).thenReturn(flowOf(tasks))
        
        // When
        viewModel = ProfileViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(0f, viewModel.uiState.value.statistics.completionRate, 0.01f)
    }
    
    @Test
    fun `weekly data has 7 days`() = runTest {
        // Given
        whenever(repository.getAllTasks()).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel = ProfileViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(7, viewModel.uiState.value.statistics.weeklyData.size)
    }
    
    @Test
    fun `clearError clears error message`() = runTest {
        // Given
        whenever(repository.getAllTasks()).thenReturn(flowOf(emptyList()))
        viewModel = ProfileViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
