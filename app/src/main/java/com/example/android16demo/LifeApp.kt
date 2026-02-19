package com.example.android16demo

import android.app.Application
import com.example.android16demo.data.repository.WebRepository
import com.example.android16demo.data.sync.SyncPreferences

/**
 * Application class for Life App.
 * Provides singleton instances for preferences and web repository.
 */
class LifeApp : Application() {

    val syncPreferences: SyncPreferences by lazy {
        SyncPreferences(this)
    }

    val webRepository: WebRepository by lazy {
        WebRepository(syncPreferences)
    }
}
