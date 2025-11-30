package com.example.android16demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android16demo.data.repository.TemplateRepository
import com.example.android16demo.data.sync.SyncPreferences
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
    val successMessage: String? = null
)

/**
 * ViewModel for Settings screen
 */
class SettingsViewModel(
    private val syncPreferences: SyncPreferences,
    private val templateRepository: TemplateRepository? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * Load settings from preferences
     */
    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            isLoggedIn = syncPreferences.isLoggedIn,
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
            customTags = syncPreferences.getCustomTagList()
        )
    }
    
    /**
     * Login with username and password
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoggingIn = true)
                
                // TODO: Implement actual authentication with server
                // For now, simulate a login
                kotlinx.coroutines.delay(1000)
                
                // Save credentials
                syncPreferences.username = username
                syncPreferences.userId = username
                syncPreferences.authToken = "mock_token_$username"
                
                _uiState.value = _uiState.value.copy(
                    isLoggingIn = false,
                    isLoggedIn = true,
                    username = username,
                    successMessage = "Login successful"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoggingIn = false,
                    errorMessage = e.message ?: "Login failed"
                )
            }
        }
    }
    
    /**
     * Logout and clear credentials
     */
    fun logout() {
        syncPreferences.clearAuth()
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            username = null,
            successMessage = "Logged out successfully"
        )
    }
    
    /**
     * Sync data with server
     */
    fun syncNow() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSyncing = true)
                
                // TODO: Implement actual sync with server
                kotlinx.coroutines.delay(2000)
                
                val now = System.currentTimeMillis()
                syncPreferences.lastSyncTime = now
                
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    lastSyncTime = now,
                    successMessage = "Sync completed"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = e.message ?: "Sync failed"
                )
            }
        }
    }
    
    /**
     * Toggle auto sync
     */
    fun setAutoSync(enabled: Boolean) {
        syncPreferences.autoSyncEnabled = enabled
        _uiState.value = _uiState.value.copy(autoSyncEnabled = enabled)
    }
    
    /**
     * Toggle wifi only sync
     */
    fun setWifiOnly(enabled: Boolean) {
        syncPreferences.syncOnWifiOnly = enabled
        _uiState.value = _uiState.value.copy(syncOnWifiOnly = enabled)
    }
    
    /**
     * Update server URL
     */
    fun updateServerUrl(url: String) {
        syncPreferences.serverUrl = url
        _uiState.value = _uiState.value.copy(serverUrl = url)
    }
    
    /**
     * Update server password
     */
    fun updateServerPassword(password: String) {
        syncPreferences.serverPassword = password
        _uiState.value = _uiState.value.copy(serverPassword = password)
    }
    
    /**
     * Update push template
     */
    fun updatePushTemplate(templateId: String?) {
        syncPreferences.pushTemplateId = templateId
        _uiState.value = _uiState.value.copy(pushTemplateId = templateId)
    }
    
    /**
     * Update theme mode
     */
    fun updateThemeMode(mode: String) {
        syncPreferences.themeMode = mode
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }
    
    /**
     * Update language
     */
    fun updateLanguage(language: String) {
        syncPreferences.language = language
        _uiState.value = _uiState.value.copy(language = language)
    }
    
    /**
     * Add custom tag
     */
    fun addCustomTag(tag: String) {
        syncPreferences.addCustomTag(tag)
        _uiState.value = _uiState.value.copy(customTags = syncPreferences.getCustomTagList())
    }
    
    /**
     * Remove custom tag
     */
    fun removeCustomTag(tag: String) {
        syncPreferences.removeCustomTag(tag)
        _uiState.value = _uiState.value.copy(customTags = syncPreferences.getCustomTagList())
    }
    
    /**
     * Clear error/success messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
