package com.example.android16demo.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.android16demo.LifeApp
import com.example.android16demo.MainActivity
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Worker for sending daily summary notifications.
 * Shows an overview of tasks for the day every morning.
 */
class DailySummaryWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val CHANNEL_ID = "daily_summary"
        const val CHANNEL_NAME = "Daily Summary"
        const val WORK_NAME = "daily_summary_work"
        private const val NOTIFICATION_ID = 2000
        
        /**
         * Schedule daily summary notification at 8:00 AM
         */
        fun scheduleDailySummary(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(
                1, TimeUnit.DAYS
            ).setInitialDelay(
                calculateInitialDelay(),
                TimeUnit.MILLISECONDS
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        
        /**
         * Cancel daily summary notifications
         */
        fun cancelDailySummary(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
        
        /**
         * Calculate delay until next 8:00 AM
         */
        private fun calculateInitialDelay(): Long {
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            
            // Set to 8:00 AM
            calendar.set(Calendar.HOUR_OF_DAY, 8)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // If 8 AM has passed today, schedule for tomorrow
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            return calendar.timeInMillis - now
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            val app = context.applicationContext as? LifeApp
            val repository = app?.taskRepository ?: return Result.failure()
            
            val activeTasks = repository.getActiveTasks().first()
            val todayTasks = activeTasks.filter { task ->
                val deadline = task.deadline ?: return@filter false
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = deadline
                
                val today = Calendar.getInstance()
                calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
            
            val overdueTasks = activeTasks.filter { it.isOverdue() }
            
            createNotificationChannel()
            showSummaryNotification(activeTasks.size, todayTasks.size, overdueTasks.size)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily task summary notifications"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showSummaryNotification(
        totalTasks: Int,
        todayTasks: Int,
        overdueTasks: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = "📋 Good Morning!"
        val content = buildString {
            if (overdueTasks > 0) {
                append("⚠️ $overdueTasks overdue. ")
            }
            append("📅 $todayTasks due today. ")
            append("📝 $totalTasks total tasks.")
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
