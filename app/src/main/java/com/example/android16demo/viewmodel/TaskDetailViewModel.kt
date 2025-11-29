package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Task Detail Screen (Create/Edit)
 */
data class TaskDetailUiState(
    val task: Task? = null,
    val title: String = "",
    val description: String = "",
    val startTime: Long? = null,
    val deadline: Long? = null,
    val priority: Int = Task.PRIORITY_MEDIUM,
    val progress: Float = 0f,
    val isPublic: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val titleError: String? = null
)

/**
 * ViewModel for Task Detail Screen (Create/Edit)
 */
class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val taskId: String? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    
    val isEditMode: Boolean = taskId != null
    
    init {
        if (taskId != null) {
            loadTask(taskId)
        }
    }
    
    /**
     * Load existing task for editing
     */
    private fun loadTask(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val task = repository.getTaskByIdOnce(id)
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        task = task,
                        title = task.title,
                        description = task.description ?: "",
                        startTime = task.startTime,
                        deadline = task.deadline,
                        priority = task.priority,
                        progress = task.progress,
                        isPublic = task.isPublic,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Task not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load task"
                )
            }
        }
    }
    
    /**
     * Update title
     */
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = if (title.isBlank()) "Title is required" else null
        )
    }
    
    /**
     * Update description
     */
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    /**
     * Update start time
     */
    fun updateStartTime(startTime: Long?) {
        _uiState.value = _uiState.value.copy(startTime = startTime)
    }
    
    /**
     * Update deadline
     */
    fun updateDeadline(deadline: Long?) {
        _uiState.value = _uiState.value.copy(deadline = deadline)
    }
    
    /**
     * Update priority
     */
    fun updatePriority(priority: Int) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }
    
    /**
     * Update progress
     */
    fun updateProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(progress = progress.coerceIn(0f, 1f))
    }
    
    /**
     * Update public visibility
     */
    fun updateIsPublic(isPublic: Boolean) {
        _uiState.value = _uiState.value.copy(isPublic = isPublic)
    }
    
    /**
     * Validate form
     */
    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true
        
        if (state.title.isBlank()) {
            _uiState.value = _uiState.value.copy(titleError = "Title is required")
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Save the task (Push to stack)
     */
    fun saveTask() {
        if (!validateForm()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val state = _uiState.value
                
                val task = if (isEditMode && state.task != null) {
                    // Update existing task
                    state.task.copy(
                        title = state.title.trim(),
                        description = state.description.trim().ifBlank { null },
                        startTime = state.startTime,
                        deadline = state.deadline,
                        priority = state.priority,
                        progress = state.progress,
                        isPublic = state.isPublic
                    )
                } else {
                    // Create new task
                    Task(
                        title = state.title.trim(),
                        description = state.description.trim().ifBlank { null },
                        startTime = state.startTime,
                        deadline = state.deadline,
                        priority = state.priority,
                        progress = state.progress,
                        isPublic = state.isPublic
                    )
                }
                
                if (isEditMode) {
                    repository.updateTask(task)
                } else {
                    repository.pushTask(task)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to save task"
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
