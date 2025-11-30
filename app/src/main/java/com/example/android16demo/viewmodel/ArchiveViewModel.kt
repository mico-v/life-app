package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * UI State for the Archive Screen
 */
data class ArchiveUiState(
    val archivedTasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val allTags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for the Archive Screen (Completed/Popped tasks)
 */
class ArchiveViewModel(private val repository: TaskRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTag = MutableStateFlow<String?>(null)
    
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
                    // Extract all unique tags from tasks
                    val allTags = tasks.flatMap { it.getTagList() }.distinct().sorted()
                    
                    _uiState.value = _uiState.value.copy(
                        archivedTasks = tasks,
                        allTags = allTags,
                        isLoading = false,
                        errorMessage = null
                    )
                    
                    // Apply filters
                    applyFilters()
                }
        }
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    /**
     * Update selected tag filter
     */
    fun updateSelectedTag(tag: String?) {
        _selectedTag.value = tag
        _uiState.value = _uiState.value.copy(selectedTag = tag)
        applyFilters()
    }
    
    /**
     * Apply search and tag filters
     */
    private fun applyFilters() {
        val tasks = _uiState.value.archivedTasks
        val query = _searchQuery.value.lowercase().trim()
        val tag = _selectedTag.value
        
        val filtered = tasks.filter { task ->
            val matchesSearch = query.isEmpty() || 
                task.title.lowercase().contains(query) ||
                (task.description?.lowercase()?.contains(query) == true)
            
            val matchesTag = tag == null || task.hasTag(tag)
            
            matchesSearch && matchesTag
        }
        
        _uiState.value = _uiState.value.copy(filteredTasks = filtered)
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
