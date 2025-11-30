# 04 - ViewModel 层 (ViewModel Layer)

## 概述

ViewModel 层负责管理 UI 状态和处理业务逻辑。使用 StateFlow 实现响应式状态管理。

---

## ViewModelFactory

### ViewModelFactory.kt

**路径:** `viewmodel/ViewModelFactory.kt`

由于未使用 Hilt，需要手动创建 ViewModel 工厂。

```kotlin
class ViewModelFactory(
    private val repository: TaskRepository,
    private val taskId: String? = null
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ArchiveViewModel::class.java) -> {
                ArchiveViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TaskDetailViewModel::class.java) -> {
                TaskDetailViewModel(repository, taskId) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
```

### 使用方式
```kotlin
val app = LocalContext.current.applicationContext as LifeApp
val viewModel: HomeViewModel = viewModel(
    factory = ViewModelFactory(app.taskRepository)
)
```

---

## HomeViewModel

### HomeViewModel.kt

**路径:** `viewmodel/HomeViewModel.kt`

管理主页（任务队列）的状态。

#### UI State
```kotlin
data class HomeUiState(
    val activeTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
```

#### ViewModel 实现
```kotlin
class HomeViewModel(private val repository: TaskRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadActiveTasks()
    }
    
    // 加载活跃任务
    private fun loadActiveTasks() {
        viewModelScope.launch {
            repository.getActiveTasks()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
                .collect { tasks ->
                    _uiState.value = _uiState.value.copy(
                        activeTasks = tasks,
                        isLoading = false
                    )
                }
        }
    }
    
    // Pop (完成) 任务
    fun popTask(taskId: String) {
        viewModelScope.launch {
            try {
                repository.popTask(taskId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to complete task: ${e.message}"
                )
            }
        }
    }
    
    // 删除任务
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTaskById(taskId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete task: ${e.message}"
                )
            }
        }
    }
    
    // 更新进度
    fun updateTaskProgress(taskId: String, progress: Float)
    
    // 清除错误消息
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
```

---

## ArchiveViewModel

### ArchiveViewModel.kt

**路径:** `viewmodel/ArchiveViewModel.kt`

管理归档页面（已完成任务）的状态。

#### UI State
```kotlin
data class ArchiveUiState(
    val archivedTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
```

#### 核心方法
```kotlin
class ArchiveViewModel(private val repository: TaskRepository) : ViewModel() {
    
    // 加载归档任务
    private fun loadArchivedTasks()
    
    // 永久删除任务
    fun deleteTask(taskId: String)
    
    // 清空所有归档
    fun clearAllArchived() {
        viewModelScope.launch {
            try {
                repository.clearArchivedTasks()
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }
    
    fun clearError()
}
```

---

## TaskDetailViewModel

### TaskDetailViewModel.kt

**路径:** `viewmodel/TaskDetailViewModel.kt`

管理任务详情页面（创建/编辑）的状态。

#### UI State
```kotlin
data class TaskDetailUiState(
    val task: Task? = null,           // 编辑时的原始任务
    val title: String = "",           // 标题输入
    val description: String = "",     // 描述输入
    val startTime: Long? = null,      // 开始时间
    val deadline: Long? = null,       // 截止时间
    val priority: Int = Task.PRIORITY_MEDIUM,
    val progress: Float = 0f,
    val isPublic: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,     // 保存成功标志
    val errorMessage: String? = null,
    val titleError: String? = null    // 标题验证错误
)
```

#### ViewModel 实现
```kotlin
class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val taskId: String? = null
) : ViewModel() {
    
    val isEditMode: Boolean = taskId != null
    
    init {
        if (taskId != null) {
            loadTask(taskId)
        }
    }
    
    // 加载现有任务 (编辑模式)
    private fun loadTask(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val task = repository.getTaskByIdOnce(id)
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        task = task,
                        title = task.title,
                        description = task.description ?: "",
                        startTime = task.startTime,
                        deadline = task.deadline,
                        priority = task.priority,
                        progress = task.progress,
                        isPublic = task.isPublic,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }
    
    // 更新各字段
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = if (title.isBlank()) "Title is required" else null
        )
    }
    
    fun updateDescription(description: String)
    fun updateStartTime(startTime: Long?)
    fun updateDeadline(deadline: Long?)
    fun updatePriority(priority: Int)
    fun updateProgress(progress: Float)
    fun updateIsPublic(isPublic: Boolean)
    
    // 表单验证
    private fun validateForm(): Boolean {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = _uiState.value.copy(titleError = "Title is required")
            return false
        }
        return true
    }
    
    // 保存任务 (Push)
    // 注意: 此方法同时处理创建和更新逻辑
    // 重构建议: 可以拆分为 createTask() 和 updateTask() 两个私有方法
    // 但当前实现更简洁，因为两者共享相同的 UI 流程
    fun saveTask() {
        if (!validateForm()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val state = _uiState.value
                
                val task = if (isEditMode && state.task != null) {
                    // 更新现有任务
                    state.task.copy(
                        title = state.title.trim(),
                        description = state.description.trim().ifBlank { null },
                        startTime = state.startTime,
                        deadline = state.deadline,
                        priority = state.priority,
                        progress = state.progress,
                        isPublic = state.isPublic
                    )
                } else {
                    // 创建新任务
                    Task(
                        title = state.title.trim(),
                        description = state.description.trim().ifBlank { null },
                        startTime = state.startTime,
                        deadline = state.deadline,
                        priority = state.priority,
                        progress = state.progress,
                        isPublic = state.isPublic
                    )
                }
                
                if (isEditMode) {
                    repository.updateTask(task)
                } else {
                    repository.pushTask(task)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true  // 触发导航返回
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}
```

---

## ProfileViewModel

### ProfileViewModel.kt

**路径:** `viewmodel/ProfileViewModel.kt`

管理个人中心/统计页面的状态。

#### 统计数据模型
```kotlin
data class TaskStatistics(
    val completedToday: Int = 0,
    val completedThisWeek: Int = 0,
    val activeTasks: Int = 0,
    val totalCompletedAllTime: Int = 0,
    val completionRate: Float = 0f,      // 百分比
    val weeklyData: List<DayStats> = emptyList()
)

data class DayStats(
    val dayOfWeek: String,  // "Mon", "Tue", etc.
    val completedCount: Int
)
```

#### UI State
```kotlin
data class ProfileUiState(
    val statistics: TaskStatistics = TaskStatistics(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
```

#### 核心方法
```kotlin
class ProfileViewModel(private val repository: TaskRepository) : ViewModel() {
    
    init {
        loadStatistics()
    }
    
    // 加载统计数据
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val allTasks = repository.getAllTasks().first()
                val completedTasks = allTasks.filter { it.isCompleted }
                val activeTasks = allTasks.filter { !it.isCompleted }
                
                // 计算今日完成
                val completedToday = completedTasks.count { 
                    (it.completedAt ?: 0) >= startOfToday 
                }
                
                // 计算本周完成
                val completedThisWeek = completedTasks.count { 
                    (it.completedAt ?: 0) >= startOfWeek 
                }
                
                // 计算按时完成率
                val completionRate = calculateCompletionRate(completedTasks)
                
                // 计算过去7天数据
                val weeklyData = calculateWeeklyData(completedTasks)
                
                _uiState.value = _uiState.value.copy(
                    statistics = TaskStatistics(...),
                    isLoading = false
                )
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }
    
    // 计算过去7天每天的完成数
    private fun calculateWeeklyData(completedTasks: List<Task>): List<DayStats>
    
    // 刷新统计
    fun refresh() {
        loadStatistics()
    }
}
```

---

## 状态管理模式

### 单向数据流

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    UI       │────▶│  ViewModel  │────▶│ Repository  │
│  (Compose)  │     │             │     │             │
└─────────────┘     └─────────────┘     └─────────────┘
      ▲                    │                    │
      │                    │                    │
      │              StateFlow                  │
      │                    │                    │
      └────────────────────┘◀───────Flow────────┘
```

### 最佳实践

1. **UI State 不可变** - 使用 `data class` 和 `copy()`
2. **使用 StateFlow** - 替代 LiveData
3. **错误处理统一** - 通过 `errorMessage` 字段
4. **加载状态管理** - 通过 `isLoading` 字段
5. **成功回调** - 通过 `isSaved` 等标志字段

### 在 UI 中使用

```kotlin
@Composable
fun SomeScreen() {
    val viewModel: SomeViewModel = viewModel(...)
    val uiState by viewModel.uiState.collectAsState()
    
    // 显示加载指示器
    if (uiState.isLoading) {
        CircularProgressIndicator()
    }
    
    // 显示错误消息
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }
    
    // 响应成功保存
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }
}
```

---

## 相关文件

- `viewmodel/HomeViewModel.kt`
- `viewmodel/ArchiveViewModel.kt`
- `viewmodel/TaskDetailViewModel.kt`
- `viewmodel/ProfileViewModel.kt`
- `viewmodel/ViewModelFactory.kt`
