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
 * UI State for the Archive Screen
 */
data class ArchiveUiState(
    val archivedTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for the Archive Screen (Completed/Popped tasks)
 */
class ArchiveViewModel(private val repository: TaskRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()
    
    init {
        loadArchivedTasks()
    }
    
    /**
     * Load archived tasks from repository
     */
    private fun loadArchivedTasks() {
        viewModelScope.launch {
            repository.getArchivedTasks()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { tasks ->
                    _uiState.value = _uiState.value.copy(
                        archivedTasks = tasks,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
    
    /**
     * Delete a task permanently
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
     * Clear all archived tasks
     */
    fun clearAllArchived() {
        viewModelScope.launch {
            try {
                repository.clearArchivedTasks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to clear archive: ${e.message}"
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
