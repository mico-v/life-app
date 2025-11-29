package com.example.android16demo.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.android16demo.MainActivity
import java.util.concurrent.TimeUnit

/**
 * Worker for sending task deadline reminder notifications.
 * Schedules notifications 15 minutes or 1 hour before the deadline.
 */
class DeadlineReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val CHANNEL_ID = "deadline_reminders"
        const val CHANNEL_NAME = "Deadline Reminders"
        
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_MINUTES_BEFORE = "minutes_before"
        
        private const val NOTIFICATION_ID_BASE = 1000
        
        /**
         * Schedule a deadline reminder for a task
         */
        fun scheduleReminder(
            context: Context,
            taskId: String,
            taskTitle: String,
            deadline: Long,
            minutesBefore: Int = 15
        ) {
            val currentTime = System.currentTimeMillis()
            val reminderTime = deadline - (minutesBefore * 60 * 1000L)
            
            // Only schedule if reminder time is in the future
            if (reminderTime <= currentTime) return
            
            val delay = reminderTime - currentTime
            
            val inputData = Data.Builder()
                .putString(KEY_TASK_ID, taskId)
                .putString(KEY_TASK_TITLE, taskTitle)
                .putInt(KEY_MINUTES_BEFORE, minutesBefore)
                .build()
            
            val workRequest = OneTimeWorkRequestBuilder<DeadlineReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()
            
            // Use unique work name to allow cancellation
            val workName = "reminder_${taskId}_${minutesBefore}"
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
        
        /**
         * Cancel reminders for a task
         */
        fun cancelReminder(context: Context, taskId: String) {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_${taskId}_15")
            WorkManager.getInstance(context).cancelUniqueWork("reminder_${taskId}_60")
        }
        
        /**
         * Schedule both 15-minute and 1-hour reminders
         */
        fun scheduleAllReminders(
            context: Context,
            taskId: String,
            taskTitle: String,
            deadline: Long
        ) {
            scheduleReminder(context, taskId, taskTitle, deadline, 15)
            scheduleReminder(context, taskId, taskTitle, deadline, 60)
        }
    }
    
    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: return Result.failure()
        val minutesBefore = inputData.getInt(KEY_MINUTES_BEFORE, 15)
        
        createNotificationChannel()
        showNotification(taskId, taskTitle, minutesBefore)
        
        return Result.success()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for task deadlines"
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(taskId: String, taskTitle: String, minutesBefore: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val timeText = when (minutesBefore) {
            15 -> "15 minutes"
            60 -> "1 hour"
            else -> "$minutesBefore minutes"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Deadline Approaching")
            .setContentText("\"$taskTitle\" is due in $timeText")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        
        val notificationId = NOTIFICATION_ID_BASE + taskId.hashCode() + minutesBefore
        notificationManager.notify(notificationId, notification)
    }
}
