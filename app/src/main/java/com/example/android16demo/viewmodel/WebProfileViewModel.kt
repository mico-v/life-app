package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android16demo.data.repository.WebRepository
import com.example.android16demo.data.sync.SyncPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WebProfileUiState(
    val loading: Boolean = false,
    val displayName: String = "",
    val motto: String = "",
    val totalPosts: Int = 0,
    val activeSources: Int = 0,
    val primaryStatus: String = "Offline",
    val error: String? = null,
    val saved: Boolean = false
)

class WebProfileViewModel(
    private val webRepository: WebRepository,
    private val syncPreferences: SyncPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WebProfileUiState(
            loading = true,
            displayName = syncPreferences.userDisplayName,
            motto = syncPreferences.userMotto
        )
    )
    val uiState: StateFlow<WebProfileUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { it.copy(displayName = value, saved = false) }
    }

    fun onMottoChanged(value: String) {
        _uiState.update { it.copy(motto = value, saved = false) }
    }

    fun saveLocalProfile() {
        syncPreferences.userDisplayName = _uiState.value.displayName.trim().ifEmpty { "Life App User" }
        syncPreferences.userMotto = _uiState.value.motto.trim().ifEmpty { "记录此刻，持续更新" }
        _uiState.update { it.copy(saved = true) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            webRepository.getPublicFeed()
                .onSuccess { feed ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            totalPosts = feed.stats.totalPosts,
                            activeSources = feed.stats.activeSources,
                            primaryStatus = feed.status.primary.status
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(loading = false, error = err.message ?: "Failed to refresh profile") }
                }
        }
    }

    fun clearFlags() {
        _uiState.update { it.copy(error = null, saved = false) }
    }
}
