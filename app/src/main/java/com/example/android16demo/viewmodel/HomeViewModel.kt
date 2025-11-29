package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * UI State for the Home Screen
 */
data class HomeUiState(
    val activeTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for the Home Screen (Task Queue/Stack view)
 */
class HomeViewModel(private val repository: TaskRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadActiveTasks()
    }
    
    /**
     * Load active tasks from repository
     */
    private fun loadActiveTasks() {
        viewModelScope.launch {
            repository.getActiveTasks()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { tasks ->
                    _uiState.value = _uiState.value.copy(
                        activeTasks = tasks,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
    
    /**
     * Pop (complete) a task
     */
    fun popTask(taskId: String) {
        viewModelScope.launch {
            try {
                repository.popTask(taskId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to complete task: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Delete a task
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTaskById(taskId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete task: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update task progress
     */
    fun updateTaskProgress(taskId: String, progress: Float) {
        viewModelScope.launch {
            try {
                repository.updateTaskProgress(taskId, progress)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update progress: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
