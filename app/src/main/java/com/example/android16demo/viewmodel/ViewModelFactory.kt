package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android16demo.data.repository.TaskRepository

/**
 * Factory for creating ViewModels with TaskRepository dependency
 */
class ViewModelFactory(
    private val repository: TaskRepository,
    private val taskId: String? = null
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ArchiveViewModel::class.java) -> {
                ArchiveViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TaskDetailViewModel::class.java) -> {
                TaskDetailViewModel(repository, taskId) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
