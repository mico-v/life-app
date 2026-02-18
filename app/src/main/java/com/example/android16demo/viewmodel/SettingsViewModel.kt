package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android16demo.data.repository.TemplateRepository
import com.example.android16demo.data.sync.SyncPreferences
import com.example.android16demo.data.sync.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Settings screen UI state
 */
data class SettingsUiState(
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val lastSyncTime: Long = 0,
    val autoSyncEnabled: Boolean = false,
    val syncOnWifiOnly: Boolean = true,
    val serverUrl: String = "",
    val serverPassword: String = "",
    val clientToken: String = "",
    val pushTemplateId: String? = null,
    val themeMode: String = SyncPreferences.THEME_SYSTEM,
    val language: String = SyncPreferences.LANGUAGE_SYSTEM,
    val customTags: List<String> = emptyList(),
    val isSyncing: Boolean = false,
    val isLoggingIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSyncConfigured: Boolean = false
)

class SettingsViewModel(
    private val syncPreferences: SyncPreferences,
    private val syncRepository: SyncRepository,
    private val templateRepository: TemplateRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            isLoggedIn = syncPreferences.isSyncConfigured,
            username = syncPreferences.username,
            lastSyncTime = syncPreferences.lastSyncTime,
            autoSyncEnabled = syncPreferences.autoSyncEnabled,
            syncOnWifiOnly = syncPreferences.syncOnWifiOnly,
            serverUrl = syncPreferences.serverUrl,
            serverPassword = syncPreferences.serverPassword,
            clientToken = syncPreferences.clientToken,
            pushTemplateId = syncPreferences.pushTemplateId,
            themeMode = syncPreferences.themeMode,
            language = syncPreferences.language,
            customTags = syncPreferences.getCustomTagList(),
            isSyncConfigured = syncPreferences.isSyncConfigured
        )
    }

    fun login(username: String, password: String) {
        // legacy no-op: current protocol uses client-token + server-password.
        syncPreferences.username = username
        _uiState.value = _uiState.value.copy(
            username = username,
            successMessage = "Profile updated"
        )
    }

    fun logout() {
        syncPreferences.clearAuth()
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            username = null,
            successMessage = "Cleared local auth state"
        )
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            when (val result = syncRepository.syncTasks()) {
                is SyncRepository.SyncResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        lastSyncTime = result.serverTime,
                        successMessage = "Sync completed (${result.syncedCount})"
                    )
                }
                is SyncRepository.SyncResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        errorMessage = result.message
                    )
                }
                SyncRepository.SyncResult.NotConfigured -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        errorMessage = "Please configure server URL and password first"
                    )
                }
            }
        }
    }

    fun setAutoSync(enabled: Boolean) {
        syncPreferences.autoSyncEnabled = enabled
        _uiState.value = _uiState.value.copy(autoSyncEnabled = enabled)
    }

    fun setWifiOnly(enabled: Boolean) {
        syncPreferences.syncOnWifiOnly = enabled
        _uiState.value = _uiState.value.copy(syncOnWifiOnly = enabled)
    }

    fun updateServerUrl(url: String) {
        syncPreferences.serverUrl = url
        _uiState.value = _uiState.value.copy(
            serverUrl = url,
            isSyncConfigured = syncPreferences.isSyncConfigured,
            isLoggedIn = syncPreferences.isSyncConfigured
        )
    }

    fun updateServerPassword(password: String) {
        syncPreferences.serverPassword = password
        _uiState.value = _uiState.value.copy(
            serverPassword = password,
            isSyncConfigured = syncPreferences.isSyncConfigured,
            isLoggedIn = syncPreferences.isSyncConfigured
        )
    }

    fun updatePushTemplate(templateId: String?) {
        syncPreferences.pushTemplateId = templateId
        _uiState.value = _uiState.value.copy(pushTemplateId = templateId)
    }

    fun updateThemeMode(mode: String) {
        syncPreferences.themeMode = mode
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun updateLanguage(language: String) {
        syncPreferences.language = language
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun addCustomTag(tag: String) {
        syncPreferences.addCustomTag(tag)
        _uiState.value = _uiState.value.copy(customTags = syncPreferences.getCustomTagList())
    }

    fun removeCustomTag(tag: String) {
        syncPreferences.removeCustomTag(tag)
        _uiState.value = _uiState.value.copy(customTags = syncPreferences.getCustomTagList())
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
