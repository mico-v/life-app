# 02 - 数据层 (Data Layer)

## 概述

数据层负责本地数据持久化，使用 Room 数据库。包含实体定义、DAO 接口和 Repository 封装。

---

## 数据库配置

### AppDatabase.kt

**路径:** `data/AppDatabase.kt`

```kotlin
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
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
    }
}
```

### 特性
- **单例模式:** 双重检查锁定
- **数据库回调:** 首次创建时插入默认模板
- **迁移策略:** `fallbackToDestructiveMigration()` (开发阶段)

---

## 实体定义 (Entities)

### Task.kt

**路径:** `data/entity/Task.kt`

```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,                    // 任务标题 (必填)
    val description: String? = null,      // 描述 (可选)
    val createdAt: Long = System.currentTimeMillis(),
    val startTime: Long? = null,          // 计划开始时间
    val deadline: Long? = null,           // 截止时间 (DDL)
    val isCompleted: Boolean = false,     // 是否已完成 (Pop)
    val completedAt: Long? = null,        // 完成时间戳
    val progress: Float = 0f,             // 进度 0.0 - 1.0
    val priority: Int = 1,                // 优先级 1=Low, 2=Medium, 3=High
    val isPublic: Boolean = false         // 是否公开 (用于服务器同步)
)
```

#### 辅助方法
```kotlin
companion object {
    const val PRIORITY_LOW = 1
    const val PRIORITY_MEDIUM = 2
    const val PRIORITY_HIGH = 3
}

// 检查是否逾期
fun isOverdue(): Boolean {
    return deadline != null && !isCompleted && System.currentTimeMillis() > deadline
}

// 获取优先级标签
fun getPriorityLabel(): String {
    return when (priority) {
        PRIORITY_LOW -> "Low"
        PRIORITY_MEDIUM -> "Medium"
        PRIORITY_HIGH -> "High"
        else -> "Unknown"
    }
}
```

### TaskTemplate.kt

**路径:** `data/entity/TaskTemplate.kt`

```kotlin
@Entity(tableName = "task_templates")
data class TaskTemplate(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,                     // 模板名称 (如 "Work")
    val defaultTitle: String,             // 默认任务标题
    val defaultDescription: String? = null,
    val defaultDurationMinutes: Int? = null,  // 默认时长
    val defaultPriority: Int = Task.PRIORITY_MEDIUM,
    val iconName: String = "default",     // 图标标识
    val colorHex: String = "#6650a4",     // 颜色值
    val sortOrder: Int = 0,               // 排序顺序
    val createdAt: Long = System.currentTimeMillis(),
    val isBuiltIn: Boolean = false        // 是否内置模板
)
```

#### 默认模板
```kotlin
companion object {
    fun getDefaultTemplates(): List<TaskTemplate> = listOf(
        TaskTemplate(id = "template_work", name = "Work", ...),
        TaskTemplate(id = "template_study", name = "Study", ...),
        TaskTemplate(id = "template_exercise", name = "Exercise", ...),
        TaskTemplate(id = "template_meeting", name = "Meeting", ...),
        TaskTemplate(id = "template_personal", name = "Personal", ...)
    )
}

// 从模板创建任务
fun toTask(customTitle: String? = null): Task
```

---

## DAO 接口

### TaskDao.kt

**路径:** `data/dao/TaskDao.kt`

#### 查询方法 (返回 Flow)

| 方法 | 说明 |
|------|------|
| `getAllTasks()` | 获取所有任务，按创建时间降序 |
| `getActiveTasks()` | 获取未完成任务，按 DDL 排序 |
| `getArchivedTasks()` | 获取已完成任务，按完成时间降序 |
| `getTaskById(id)` | 获取单个任务 |
| `getTasksInTimeRange(start, end)` | 获取时间范围内的任务 |
| `getTodayTasks(todayMillis)` | 获取今日任务 |
| `getOverdueTasks(currentTime)` | 获取逾期任务 |
| `getPublicTasks()` | 获取公开任务 |

#### 操作方法 (suspend)

| 方法 | 说明 |
|------|------|
| `insertTask(task)` | 插入任务 |
| `insertTasks(tasks)` | 批量插入 |
| `updateTask(task)` | 更新任务 |
| `deleteTask(task)` | 删除任务 |
| `deleteTaskById(id)` | 按 ID 删除 |
| `deleteAllArchivedTasks()` | 清空归档 |
| `completeTask(id, completedAt)` | 标记完成 |
| `updateTaskProgress(id, progress)` | 更新进度 |

#### 统计方法 (suspend)

| 方法 | 说明 |
|------|------|
| `getCompletedTaskCountSince(since)` | 某时间后完成的任务数 |
| `getActiveTaskCount()` | 活跃任务数量 |

### TaskTemplateDao.kt

**路径:** `data/dao/TaskTemplateDao.kt`

| 方法 | 说明 |
|------|------|
| `getAllTemplates()` | 获取所有模板 |
| `getBuiltInTemplates()` | 获取内置模板 |
| `getUserTemplates()` | 获取用户模板 |
| `insertTemplate(template)` | 插入模板 |
| `insertTemplates(templates)` | 批量插入 |
| `deleteTemplate(template)` | 删除模板 |
| `getBuiltInTemplateCount()` | 内置模板数量 |

---

## Repository 层

### TaskRepository.kt

**路径:** `data/repository/TaskRepository.kt`

封装 DAO 操作，为 ViewModel 提供干净的 API。

#### 核心方法

```kotlin
class TaskRepository(private val taskDao: TaskDao) {
    
    // 获取活跃任务 (Flow)
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    
    // 获取归档任务 (Flow)
    fun getArchivedTasks(): Flow<List<Task>> = taskDao.getArchivedTasks()
    
    // Push 新任务
    suspend fun pushTask(task: Task) = taskDao.insertTask(task)
    
    // Pop 任务 (标记完成)
    suspend fun popTask(id: String) = 
        taskDao.completeTask(id, System.currentTimeMillis())
    
    // 删除任务
    suspend fun deleteTaskById(id: String) = taskDao.deleteTaskById(id)
    
    // 更新进度
    suspend fun updateTaskProgress(id: String, progress: Float) = 
        taskDao.updateTaskProgress(id, progress.coerceIn(0f, 1f))
    
    // 带验证的创建任务
    suspend fun createTask(
        title: String,
        description: String? = null,
        startTime: Long? = null,
        deadline: Long? = null,
        priority: Int = Task.PRIORITY_MEDIUM,
        isPublic: Boolean = false
    ): Result<Task>
}
```

### TemplateRepository.kt

**路径:** `data/repository/TemplateRepository.kt`

```kotlin
class TemplateRepository(private val templateDao: TaskTemplateDao) {
    
    fun getAllTemplates(): Flow<List<TaskTemplate>>
    
    suspend fun createTemplate(template: TaskTemplate)
    
    // 从模板创建任务
    suspend fun createTaskFromTemplate(
        templateId: String, 
        customTitle: String? = null
    ): Task?
}
```

---

## 数据流示例

### 加载活跃任务

```
HomeViewModel
    │
    └── repository.getActiveTasks()
            │
            └── taskDao.getActiveTasks()
                    │
                    └── Room Query → Flow<List<Task>>
                            │
                            └── StateFlow → UI 更新
```

### 完成任务 (Pop)

```
UI (Swipe Right)
    │
    └── viewModel.popTask(taskId)
            │
            └── repository.popTask(taskId)
                    │
                    └── taskDao.completeTask(taskId, timestamp)
                            │
                            └── Room 更新 → Flow 自动通知 → UI 刷新
```

---

## 最佳实践

1. **使用 Flow 进行响应式更新** - 数据变化自动反映到 UI
2. **Repository 封装 DAO** - ViewModel 不直接访问 DAO
3. **验证在 Repository 层** - 如 `title.isBlank()` 检查
4. **使用 suspend 函数** - 所有写操作都是挂起函数
5. **coerceIn 边界检查** - 确保 progress 在 0-1 范围内

---

## 相关文件

- `AppDatabase.kt` - 数据库定义
- `data/dao/TaskDao.kt` - 任务 DAO
- `data/dao/TaskTemplateDao.kt` - 模板 DAO
- `data/repository/TaskRepository.kt` - 任务仓库
- `data/repository/TemplateRepository.kt` - 模板仓库
