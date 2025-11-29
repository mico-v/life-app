package com.example.android16demo.data.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Secure storage for sync-related data including auth tokens
 */
class SyncPreferences(context: Context) {
    
    companion object {
        private const val PREF_FILE_NAME = "life_app_sync_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
        private const val KEY_SYNC_ON_WIFI_ONLY = "sync_on_wifi_only"
    }
    
    private val prefs: SharedPreferences
    
    init {
        // Use encrypted shared preferences for sensitive data
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        prefs = EncryptedSharedPreferences.create(
            PREF_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()
    
    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()
    
    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()
    
    var lastSyncTime: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()
    
    var autoSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SYNC_ENABLED, value).apply()
    
    var syncOnWifiOnly: Boolean
        get() = prefs.getBoolean(KEY_SYNC_ON_WIFI_ONLY, true)
        set(value) = prefs.edit().putBoolean(KEY_SYNC_ON_WIFI_ONLY, value).apply()
    
    val isLoggedIn: Boolean
        get() = !authToken.isNullOrEmpty() && !userId.isNullOrEmpty()
    
    fun clearAuth() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .apply()
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
