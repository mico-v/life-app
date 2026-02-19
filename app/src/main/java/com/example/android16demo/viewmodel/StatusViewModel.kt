package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android16demo.data.repository.WebRepository
import com.example.android16demo.network.model.PostDtoV2
import com.example.android16demo.network.model.StatusSourceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatusUiState(
    val loading: Boolean = false,
    val primary: StatusSourceDto? = null,
    val sources: List<StatusSourceDto> = emptyList(),
    val posts: List<PostDtoV2> = emptyList(),
    val error: String? = null,
    val updatedAt: Long? = null
)

class StatusViewModel(private val webRepository: WebRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(StatusUiState(loading = true))
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            webRepository.getPublicFeed()
                .onSuccess { feed ->
                    _uiState.value = StatusUiState(
                        loading = false,
                        primary = feed.status.primary,
                        sources = feed.status.sources,
                        posts = feed.posts,
                        updatedAt = feed.serverTime
                    )
                }
                .onFailure { err ->
                    _uiState.update { it.copy(loading = false, error = err.message ?: "Failed to load status feed") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
