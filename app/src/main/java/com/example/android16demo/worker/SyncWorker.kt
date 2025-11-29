package com.example.android16demo.worker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.android16demo.LifeApp
import com.example.android16demo.data.sync.SyncPreferences
import com.example.android16demo.data.sync.SyncRepository
import com.example.android16demo.network.RetrofitClient
import java.util.concurrent.TimeUnit

/**
 * Worker for background data synchronization
 */
class SyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME_PERIODIC = "periodic_sync"
        const val WORK_NAME_IMMEDIATE = "immediate_sync"
        
        /**
         * Schedule periodic background sync
         */
        fun schedulePeriodicSync(context: Context, intervalMinutes: Long = 30) {
            val syncPreferences = SyncPreferences(context)
            
            // Only schedule if auto sync is enabled and user is logged in
            if (!syncPreferences.autoSyncEnabled || !syncPreferences.isLoggedIn) {
                cancelPeriodicSync(context)
                return
            }
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (syncPreferences.syncOnWifiOnly) NetworkType.UNMETERED 
                    else NetworkType.CONNECTED
                )
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                intervalMinutes, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
        
        /**
         * Trigger immediate sync
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_IMMEDIATE,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
        
        /**
         * Cancel periodic sync
         */
        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            val app = context.applicationContext as? LifeApp ?: return Result.failure()
            val syncPreferences = SyncPreferences(context)
            
            if (!syncPreferences.isLoggedIn) {
                return Result.success() // Not logged in, nothing to sync
            }
            
            val syncRepository = SyncRepository(
                api = RetrofitClient.api,
                taskRepository = app.taskRepository,
                syncPreferences = syncPreferences
            )
            
            when (val result = syncRepository.syncTasks()) {
                is SyncRepository.SyncResult.Success -> {
                    Result.success()
                }
                is SyncRepository.SyncResult.Error -> {
                    // Retry on error
                    Result.retry()
                }
                SyncRepository.SyncResult.NotLoggedIn -> {
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
