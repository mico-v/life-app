package com.example.android16demo.data.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_PUSH_TEMPLATE_ID = "push_template_id"
        private const val KEY_USER_MOTTO = "user_motto"
        private const val KEY_USER_STATUS = "user_status"
        private const val KEY_USER_DISPLAY_NAME = "user_display_name"
    }
    
    private val prefs: SharedPreferences
    
    init {
        // Use encrypted shared preferences for sensitive data
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREF_FILE_NAME,
            masterKey,
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
    
    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()
    
    var pushTemplateId: String?
        get() = prefs.getString(KEY_PUSH_TEMPLATE_ID, null)
        set(value) = prefs.edit().putString(KEY_PUSH_TEMPLATE_ID, value).apply()
    
    var userMotto: String
        get() = prefs.getString(KEY_USER_MOTTO, "Push to Start, Pop to Finish") ?: "Push to Start, Pop to Finish"
        set(value) = prefs.edit().putString(KEY_USER_MOTTO, value).apply()
    
    var userStatus: String
        get() = prefs.getString(KEY_USER_STATUS, "Available") ?: "Available"
        set(value) = prefs.edit().putString(KEY_USER_STATUS, value).apply()
    
    var userDisplayName: String
        get() = prefs.getString(KEY_USER_DISPLAY_NAME, "Life App User") ?: "Life App User"
        set(value) = prefs.edit().putString(KEY_USER_DISPLAY_NAME, value).apply()
    
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
