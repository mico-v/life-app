package com.example.android16demo

import android.app.Application
import com.example.android16demo.data.AppDatabase
import com.example.android16demo.data.repository.TaskRepository
import com.example.android16demo.data.repository.TemplateRepository
import com.example.android16demo.data.sync.SyncPreferences

/**
 * Application class for Life App.
 * Provides singleton instances of database and repository.
 */
class LifeApp : Application() {
    
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
    
    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }
    
    val templateRepository: TemplateRepository by lazy {
        TemplateRepository(database.taskTemplateDao())
    }
    
    val syncPreferences: SyncPreferences by lazy {
        SyncPreferences(this)
    }
}
