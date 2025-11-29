package com.example.android16demo

import android.app.Application
import com.example.android16demo.data.AppDatabase
import com.example.android16demo.data.repository.TaskRepository

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
}
