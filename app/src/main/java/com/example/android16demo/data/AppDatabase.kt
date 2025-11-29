package com.example.android16demo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android16demo.data.dao.TaskDao
import com.example.android16demo.data.dao.TaskTemplateDao
import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.entity.TaskTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database for the Life App.
 * Single source of truth for all local data.
 */
@Database(
    entities = [Task::class, TaskTemplate::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun taskTemplateDao(): TaskTemplateDao
    
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
            )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
        }
        
        /**
         * Clear the singleton instance (useful for testing)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
    
    /**
     * Callback to populate database with default templates on creation
     */
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultTemplates(database.taskTemplateDao())
                }
            }
        }
        
        private suspend fun populateDefaultTemplates(dao: TaskTemplateDao) {
            val existingCount = dao.getBuiltInTemplateCount()
            if (existingCount == 0) {
                dao.insertTemplates(TaskTemplate.getDefaultTemplates())
            }
        }
    }
}
