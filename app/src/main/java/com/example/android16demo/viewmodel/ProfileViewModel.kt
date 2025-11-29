package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Statistics data for the profile screen
 */
data class TaskStatistics(
    val completedToday: Int = 0,
    val completedThisWeek: Int = 0,
    val activeTasks: Int = 0,
    val totalCompletedAllTime: Int = 0,
    val completionRate: Float = 0f,  // Percentage
    val weeklyData: List<DayStats> = emptyList()  // Last 7 days
)

data class DayStats(
    val dayOfWeek: String,
    val completedCount: Int
)

/**
 * UI State for Profile/Statistics screen
 */
data class ProfileUiState(
    val statistics: TaskStatistics = TaskStatistics(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for Profile/Statistics screen
 */
class ProfileViewModel(private val repository: TaskRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    /**
     * Load statistics from repository
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                
                // Start of today
                calendar.timeInMillis = now
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis
                
                // Start of this week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val startOfWeek = calendar.timeInMillis
                
                // Get all tasks
                val allTasks = repository.getAllTasks().first()
                val activeTasks = allTasks.filter { !it.isCompleted }
                val completedTasks = allTasks.filter { it.isCompleted }
                
                // Calculate stats
                val completedToday = completedTasks.count { 
                    (it.completedAt ?: 0) >= startOfToday 
                }
                val completedThisWeek = completedTasks.count { 
                    (it.completedAt ?: 0) >= startOfWeek 
                }
                
                // Calculate completion rate (tasks completed on time)
                val tasksWithDeadline = completedTasks.filter { it.deadline != null }
                val onTimeCompletions = tasksWithDeadline.count { task ->
                    val deadline = task.deadline ?: 0
                    val completedAt = task.completedAt ?: 0
                    completedAt <= deadline
                }
                val completionRate = if (tasksWithDeadline.isNotEmpty()) {
                    (onTimeCompletions.toFloat() / tasksWithDeadline.size) * 100
                } else 0f
                
                // Calculate weekly data (last 7 days)
                val weeklyData = calculateWeeklyData(completedTasks)
                
                val statistics = TaskStatistics(
                    completedToday = completedToday,
                    completedThisWeek = completedThisWeek,
                    activeTasks = activeTasks.size,
                    totalCompletedAllTime = completedTasks.size,
                    completionRate = completionRate,
                    weeklyData = weeklyData
                )
                
                _uiState.value = _uiState.value.copy(
                    statistics = statistics,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load statistics"
                )
            }
        }
    }
    
    /**
     * Calculate daily completion data for the last 7 days
     */
    private fun calculateWeeklyData(completedTasks: List<Task>): List<DayStats> {
        val calendar = Calendar.getInstance()
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val weeklyStats = mutableListOf<DayStats>()
        
        // Go back 6 days and count forward
        for (daysAgo in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dayStart = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = calendar.timeInMillis
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayName = dayNames[dayOfWeek - 1]
            
            val count = completedTasks.count { task ->
                val completedAt = task.completedAt ?: 0
                completedAt >= dayStart && completedAt < dayEnd
            }
            
            weeklyStats.add(DayStats(dayName, count))
        }
        
        return weeklyStats
    }
    
    /**
     * Refresh statistics
     */
    fun refresh() {
        loadStatistics()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
