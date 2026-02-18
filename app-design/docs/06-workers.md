# 06 - 后台任务 (Workers)

## 概述

后台任务使用 WorkManager 实现，包括 DDL 提醒、每日摘要通知和数据同步。

---

## DeadlineReminderWorker

### DeadlineReminderWorker.kt

**路径:** `worker/DeadlineReminderWorker.kt`

在任务截止时间前发送提醒通知。

#### 通知渠道配置
```kotlin
const val CHANNEL_ID = "deadline_reminders"
const val CHANNEL_NAME = "Deadline Reminders"
```

#### 调度提醒

```kotlin
companion object {
    /**
     * 调度单个提醒
     * @param minutesBefore 提前多少分钟提醒 (默认15分钟)
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
        
        // 只调度未来的提醒
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
        
        // 使用唯一名称以支持取消
        val workName = "reminder_${taskId}_${minutesBefore}"
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * 调度 15 分钟和 1 小时两个提醒
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
    
    /**
     * 取消任务的所有提醒
     */
    fun cancelReminder(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_${taskId}_15")
        WorkManager.getInstance(context).cancelUniqueWork("reminder_${taskId}_60")
    }
}
```

#### Worker 执行

```kotlin
override suspend fun doWork(): Result {
    val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
    val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: return Result.failure()
    val minutesBefore = inputData.getInt(KEY_MINUTES_BEFORE, 15)
    
    createNotificationChannel()
    showNotification(taskId, taskTitle, minutesBefore)
    
    return Result.success()
}

private fun showNotification(taskId: String, taskTitle: String, minutesBefore: Int) {
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
        .setVibrate(longArrayOf(0, 250, 250, 250))
        .build()
    
    notificationManager.notify(notificationId, notification)
}
```

#### 使用示例

```kotlin
// 创建任务时调度提醒
fun createTask(task: Task) {
    taskRepository.pushTask(task)
    
    task.deadline?.let { deadline ->
        DeadlineReminderWorker.scheduleAllReminders(
            context = context,
            taskId = task.id,
            taskTitle = task.title,
            deadline = deadline
        )
    }
}

// 删除任务时取消提醒
fun deleteTask(taskId: String) {
    taskRepository.deleteTaskById(taskId)
    DeadlineReminderWorker.cancelReminder(context, taskId)
}
```

---

## DailySummaryWorker

### DailySummaryWorker.kt

**路径:** `worker/DailySummaryWorker.kt`

每天早晨 8:00 发送今日任务摘要通知。

#### 通知渠道配置
```kotlin
const val CHANNEL_ID = "daily_summary"
const val CHANNEL_NAME = "Daily Summary"
const val WORK_NAME = "daily_summary_work"
```

#### 调度每日通知

```kotlin
companion object {
    /**
     * 调度每日摘要（在 8:00 AM）
     */
    fun scheduleDailySummary(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            1, TimeUnit.DAYS  // 每天执行一次
        ).setInitialDelay(
            calculateInitialDelay(),  // 延迟到下一个 8:00 AM
            TimeUnit.MILLISECONDS
        ).build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,  // 保留已有的调度
            workRequest
        )
    }
    
    /**
     * 取消每日摘要
     */
    fun cancelDailySummary(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
    
    /**
     * 计算到下一个 8:00 AM 的延迟
     */
    private fun calculateInitialDelay(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // 设置为 8:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // 如果今天 8 AM 已过，则调度到明天
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis - now
    }
}
```

#### Worker 执行

```kotlin
override suspend fun doWork(): Result {
    return try {
        val app = context.applicationContext as? LifeApp
        val repository = app?.taskRepository ?: return Result.failure()
        
        // 获取活跃任务
        val activeTasks = repository.getActiveTasks().first()
        
        // 过滤今日任务
        val todayTasks = activeTasks.filter { task ->
            val deadline = task.deadline ?: return@filter false
            isSameDay(deadline, System.currentTimeMillis())
        }
        
        // 获取逾期任务
        val overdueTasks = activeTasks.filter { it.isOverdue() }
        
        createNotificationChannel()
        showSummaryNotification(
            totalTasks = activeTasks.size,
            todayTasks = todayTasks.size,
            overdueTasks = overdueTasks.size
        )
        
        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }
}

private fun showSummaryNotification(totalTasks: Int, todayTasks: Int, overdueTasks: Int) {
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
        .build()
    
    notificationManager.notify(NOTIFICATION_ID, notification)
}
```

#### 在 MainActivity 中启动

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 调度每日摘要
        DailySummaryWorker.scheduleDailySummary(this)
        
        // ...
    }
}
```

---

## SyncWorker

### SyncWorker.kt

**路径:** `worker/SyncWorker.kt`

后台数据同步任务。

#### 调度周期同步

```kotlin
companion object {
    const val WORK_NAME_PERIODIC = "periodic_sync"
    const val WORK_NAME_IMMEDIATE = "immediate_sync"
    
    /**
     * 调度周期性后台同步
     * @param intervalMinutes 同步间隔（默认30分钟）
     */
    fun schedulePeriodicSync(context: Context, intervalMinutes: Long = 30) {
        val syncPreferences = SyncPreferences(context)
        
        // 只在启用自动同步且已完成服务端配置时调度
        if (!syncPreferences.autoSyncEnabled || !syncPreferences.isSyncConfigured) {
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
     * 立即触发同步
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
     * 取消周期同步
     */
    fun cancelPeriodicSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
    }
}
```

#### Worker 执行

```kotlin
override suspend fun doWork(): Result {
    return try {
        val app = context.applicationContext as? LifeApp ?: return Result.failure()
        val syncPreferences = SyncPreferences(context)
        
        if (!syncPreferences.isSyncConfigured) {
            return Result.success()  // 未配置服务端，静默成功
        }
        
        val syncRepository = SyncRepository(
            taskRepository = app.taskRepository,
            syncPreferences = syncPreferences
        )
        
        when (val result = syncRepository.syncTasks()) {
            is SyncRepository.SyncResult.Success -> Result.success()
            is SyncRepository.SyncResult.Error -> Result.retry()  // 错误时重试
            SyncRepository.SyncResult.NotConfigured -> Result.success()
        }
    } catch (e: Exception) {
        Result.retry()
    }
}
```

---

## 通知渠道

### 权限要求

在 `AndroidManifest.xml` 中声明：
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### 创建通知渠道

```kotlin
private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH  // 或 DEFAULT
        ).apply {
            description = "Channel description"
            enableVibration(true)
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
```

---

## WorkManager 配置要点

### 约束条件

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)   // 需要网络
    .setRequiredNetworkType(NetworkType.UNMETERED)   // 需要 WiFi
    .setRequiresBatteryNotLow(true)                  // 电量不低
    .setRequiresCharging(true)                       // 充电中
    .setRequiresDeviceIdle(true)                     // 设备空闲
    .build()
```

### 唯一工作策略

```kotlin
// 周期性工作
ExistingPeriodicWorkPolicy.KEEP      // 保留已有的，不替换
ExistingPeriodicWorkPolicy.UPDATE    // 更新为新配置
ExistingPeriodicWorkPolicy.REPLACE   // 取消并替换

// 一次性工作
ExistingWorkPolicy.KEEP              // 保留已有的
ExistingWorkPolicy.REPLACE           // 替换
ExistingWorkPolicy.APPEND            // 追加
```

### 重试策略

```kotlin
override suspend fun doWork(): Result {
    return try {
        // 执行工作
        Result.success()
    } catch (e: Exception) {
        if (runAttemptCount < 3) {
            Result.retry()  // 重试
        } else {
            Result.failure()  // 超过重试次数，失败
        }
    }
}
```

---

## 相关文件

- `worker/DeadlineReminderWorker.kt` - DDL 提醒
- `worker/DailySummaryWorker.kt` - 每日摘要
- `worker/SyncWorker.kt` - 后台同步
- `AndroidManifest.xml` - 权限声明
