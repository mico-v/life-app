package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android16demo.data.repository.TaskRepository
import com.example.android16demo.data.repository.TemplateRepository
import com.example.android16demo.data.sync.SyncPreferences

/**
 * Factory for creating ViewModels with TaskRepository dependency
 */
class ViewModelFactory(
    private val repository: TaskRepository,
    private val taskId: String? = null,
    private val syncPreferences: SyncPreferences? = null,
    private val templateRepository: TemplateRepository? = null
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
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository, syncPreferences) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                requireNotNull(syncPreferences) { "syncPreferences is required for SettingsViewModel" }
                SettingsViewModel(syncPreferences, templateRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
