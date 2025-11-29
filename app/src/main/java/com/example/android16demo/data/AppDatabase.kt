package com.example.android16demo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.android16demo.data.dao.TaskDao
import com.example.android16demo.data.entity.Task

/**
 * Room Database for the Life App.
 * Single source of truth for all local data.
 */
@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    
    companion object {
        private const val DATABASE_NAME = "life_app_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Get the singleton instance of the database.
         * Uses double-checked locking for thread safety.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
        
        /**
         * Clear the singleton instance (useful for testing)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
