package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android16demo.data.repository.WebRepository
import com.example.android16demo.data.sync.SyncPreferences
import com.example.android16demo.network.model.PostDtoV2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PublishUiState(
    val loading: Boolean = false,
    val serverUrl: String = "",
    val clientToken: String = "",
    val serverPassword: String = "",
    val statusText: String = "",
    val statusTtlMinutes: String = "15",
    val postContent: String = "",
    val postTags: String = "",
    val postLocation: String = "",
    val posts: List<PostDtoV2> = emptyList(),
    val message: String? = null,
    val error: String? = null
)

class PublishViewModel(
    private val webRepository: WebRepository,
    private val syncPreferences: SyncPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PublishUiState(
            serverUrl = syncPreferences.serverUrl,
            clientToken = syncPreferences.clientToken,
            serverPassword = syncPreferences.serverPassword
        )
    )
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    init {
        refreshPosts()
    }

    fun onServerUrlChanged(value: String) = _uiState.update { it.copy(serverUrl = value) }
    fun onClientTokenChanged(value: String) = _uiState.update { it.copy(clientToken = value) }
    fun onServerPasswordChanged(value: String) = _uiState.update { it.copy(serverPassword = value) }
    fun onStatusChanged(value: String) = _uiState.update { it.copy(statusText = value) }
    fun onTtlChanged(value: String) = _uiState.update { it.copy(statusTtlMinutes = value) }
    fun onPostContentChanged(value: String) = _uiState.update { it.copy(postContent = value) }
    fun onPostTagsChanged(value: String) = _uiState.update { it.copy(postTags = value) }
    fun onPostLocationChanged(value: String) = _uiState.update { it.copy(postLocation = value) }

    private fun persistAuth() {
        val state = _uiState.value
        syncPreferences.serverUrl = state.serverUrl.trim()
        syncPreferences.clientToken = state.clientToken.trim()
        syncPreferences.serverPassword = state.serverPassword.trim()
    }

    fun publishStatus() {
        val text = _uiState.value.statusText.trim()
        if (text.isEmpty()) {
            _uiState.update { it.copy(error = "Status cannot be empty") }
            return
        }

        viewModelScope.launch {
            persistAuth()
            _uiState.update { it.copy(loading = true, error = null, message = null) }
            val ttl = _uiState.value.statusTtlMinutes.toIntOrNull() ?: 15
            webRepository.publishStatus(text, ttl)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            loading = false,
                            statusText = "",
                            message = "Status published"
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(loading = false, error = err.message ?: "Failed to publish status") }
                }
        }
    }

    fun publishPost() {
        val content = _uiState.value.postContent.trim()
        if (content.isEmpty()) {
            _uiState.update { it.copy(error = "Post content cannot be empty") }
            return
        }

        viewModelScope.launch {
            persistAuth()
            _uiState.update { it.copy(loading = true, error = null, message = null) }
            webRepository.publishPost(
                content = content,
                tags = _uiState.value.postTags.trim().ifBlank { null },
                location = _uiState.value.postLocation.trim().ifBlank { null }
            ).onSuccess {
                _uiState.update { state ->
                    state.copy(
                        loading = false,
                        postContent = "",
                        postTags = "",
                        postLocation = "",
                        message = "Post published"
                    )
                }
                refreshPosts()
            }.onFailure { err ->
                _uiState.update { it.copy(loading = false, error = err.message ?: "Failed to publish post") }
            }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            persistAuth()
            _uiState.update { it.copy(loading = true, error = null) }
            webRepository.getMyPosts()
                .onSuccess { posts ->
                    _uiState.update { it.copy(loading = false, posts = posts) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(loading = false, error = err.message ?: "Failed to load posts") }
                }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            persistAuth()
            _uiState.update { it.copy(loading = true, error = null, message = null) }
            webRepository.deletePost(postId)
                .onSuccess {
                    _uiState.update { it.copy(loading = false, message = "Post deleted") }
                    refreshPosts()
                }
                .onFailure { err ->
                    _uiState.update { it.copy(loading = false, error = err.message ?: "Failed to delete post") }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, message = null) }
    }
}
