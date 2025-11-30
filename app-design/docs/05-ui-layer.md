# 05 - UI 层 (UI Layer)

## 概述

UI 层使用 Jetpack Compose + Material 3 构建。支持动态取色 (Dynamic Color)。

---

## 主题配置

### Theme.kt

**路径:** `ui/theme/Theme.kt`

```kotlin
@Composable
fun Android16DemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Android 12+ 动态取色
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Android 12+ 启用动态取色
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Color.kt

**路径:** `ui/theme/Color.kt`

```kotlin
// Light Theme Colors
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Dark Theme Colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
```

### Type.kt

**路径:** `ui/theme/Type.kt`

```kotlin
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(...),
    labelSmall = TextStyle(...)
)
```

---

## UI 组件

### TaskItem.kt

**路径:** `ui/components/TaskItem.kt`

任务卡片组件，支持滑动手势。

#### SwipeToDismiss 交互

```kotlin
@Composable
fun TaskItem(
    task: Task,
    onComplete: () -> Unit,  // 右滑完成
    onDelete: () -> Unit,    // 左滑删除
    onClick: () -> Unit,     // 点击编辑
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // 左滑删除
                    // 注意: onDelete() 是 ViewModel 回调，如果操作失败:
                    // 1. ViewModel 更新 errorMessage
                    // 2. Screen 通过 LaunchedEffect 显示 Snackbar
                    // 3. 由于使用 Flow 观察，列表会自动保持一致
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    // 右滑完成 - 同上，错误通过 errorMessage 处理
                    onComplete()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // 滑动背景：绿色=完成，红色=删除
            val color by animateColorAsState(
                targetValue = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                }
            )
            // 显示图标
        }
    ) {
        TaskCard(task = task, onClick = onClick)
    }
}
```

#### 错误处理流程

滑动操作的错误处理通过 ViewModel 和 StateFlow 实现：

```
用户滑动 → onComplete/onDelete() → ViewModel → Repository
                                        ↓
                               try { success } catch { errorMessage }
                                        ↓
                               StateFlow 更新 uiState.errorMessage
                                        ↓
                               LaunchedEffect 检测到变化
                                        ↓
                               Snackbar 显示错误消息
```

由于列表数据来自 `Flow<List<Task>>`，即使操作失败，UI 也会保持与数据库一致，不会出现状态不同步。

#### TaskCard 内容

```kotlin
@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    // 优先级颜色
    val priorityColor = when (task.priority) {
        Task.PRIORITY_HIGH -> MaterialTheme.colorScheme.error
        Task.PRIORITY_MEDIUM -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (task.isOverdue()) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // 标题 + 优先级指示器
            Row {
                Box(/* 优先级圆点 */)
                Text(task.title)
                if (task.isOverdue()) {
                    Icon(Icons.Default.Warning)
                }
            }
            
            // 描述
            task.description?.let { Text(it) }
            
            // 进度条
            if (task.progress > 0f && task.progress < 1f) {
                LinearProgressIndicator(progress = { task.progress })
            }
            
            // 截止时间 + 优先级标签
            Row {
                task.deadline?.let {
                    Icon(Icons.Default.Schedule)
                    Text(formatDateTime(it))
                }
                Text(task.getPriorityLabel())
            }
        }
    }
}
```

### TimelineView.kt

**路径:** `ui/components/TimelineView.kt`

时间轴视图组件，按时间分组显示任务。

#### 时间分组

```kotlin
enum class TimeSection(val displayName: String) {
    OVERDUE("Overdue"),
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    THIS_WEEK("This Week"),
    LATER("Later")
}

private fun groupTasksByTimeSection(tasks: List<Task>): Map<TimeSection, List<Task>> {
    return tasks.groupBy { task ->
        val deadline = task.deadline ?: task.startTime ?: task.createdAt
        when {
            task.isOverdue() -> TimeSection.OVERDUE
            deadline < endOfToday -> TimeSection.TODAY
            deadline < endOfTomorrow -> TimeSection.TOMORROW
            deadline < endOfWeek -> TimeSection.THIS_WEEK
            else -> TimeSection.LATER
        }
    }.toSortedMap(compareBy { it.ordinal })
}
```

#### 时间轴布局

```kotlin
@Composable
fun TimelineView(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit
) {
    val groupedTasks = groupTasksByTimeSection(tasks)
    
    LazyColumn {
        groupedTasks.forEach { (section, sectionTasks) ->
            // 分组标题
            item {
                TimelineSectionHeader(section)
            }
            
            // 任务列表（带时间轴线）
            itemsIndexed(sectionTasks) { index, task ->
                TimelineTaskItem(
                    task = task,
                    isLast = index == sectionTasks.lastIndex,
                    onClick = { onTaskClick(task.id) },
                    onComplete = { onTaskComplete(task.id) },
                    onDelete = { onTaskDelete(task.id) }
                )
            }
        }
    }
}
```

#### 时间轴任务项

```kotlin
@Composable
private fun TimelineTaskItem(
    task: Task,
    isLast: Boolean,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Row {
        // 时间轴线和圆点
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(/* 圆点 */)
            if (!isLast) {
                Box(/* 连接线 */)
            }
        }
        
        // 任务卡片
        TaskItem(
            task = task,
            onComplete = onComplete,
            onDelete = onDelete,
            onClick = onClick
        )
    }
}
```

### TemplateSelector.kt

**路径:** `ui/components/TemplateSelector.kt`

模板选择器组件，横向滚动列表。

```kotlin
@Composable
fun TemplateSelector(
    templates: List<TaskTemplate>,
    onTemplateSelected: (TaskTemplate) -> Unit
) {
    Column {
        Text("Quick Start", style = MaterialTheme.typography.labelLarge)
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(templates, key = { it.id }) { template ->
                TemplateChip(
                    template = template,
                    onClick = { onTemplateSelected(template) }
                )
            }
        }
    }
}

@Composable
private fun TemplateChip(template: TaskTemplate, onClick: () -> Unit) {
    val iconColor = Color(android.graphics.Color.parseColor(template.colorHex))
    
    Card(onClick = onClick) {
        Row {
            Box(
                modifier = Modifier.background(iconColor.copy(alpha = 0.2f))
            ) {
                Icon(getTemplateIcon(template.iconName), tint = iconColor)
            }
            Column {
                Text(template.name)
                template.defaultDurationMinutes?.let {
                    Text("$it min")
                }
            }
        }
    }
}
```

---

## 页面 (Screens)

### TaskQueueScreen.kt

**路径:** `ui/screen/TaskQueueScreen.kt`

主页 - 任务队列列表视图。

```kotlin
@Composable
fun TaskQueueScreen(
    uiState: HomeUiState,
    onTaskClick: (String) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onAddTask: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, "Push new task")
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.activeTasks.isEmpty() -> EmptyTasksMessage()
            else -> {
                LazyColumn {
                    items(uiState.activeTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onComplete = { onTaskComplete(task.id) },
                            onDelete = { onTaskDelete(task.id) },
                            onClick = { onTaskClick(task.id) }
                        )
                    }
                }
            }
        }
    }
}
```

### TimelineScreen.kt

**路径:** `ui/screen/TimelineScreen.kt`

主页 - 时间轴视图。

```kotlin
@Composable
fun TimelineScreen(
    uiState: HomeUiState,
    onTaskClick: (String) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onAddTask: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add)
            }
        }
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.activeTasks.isEmpty() -> EmptyTimelineMessage()
            else -> {
                TimelineView(
                    tasks = uiState.activeTasks,
                    onTaskClick = onTaskClick,
                    onTaskComplete = onTaskComplete,
                    onTaskDelete = onTaskDelete
                )
            }
        }
    }
}
```

### TaskDetailScreen.kt

**路径:** `ui/screen/TaskDetailScreen.kt`

任务详情 - 创建/编辑表单。

```kotlin
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    isEditMode: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartTimeChange: (Long?) -> Unit,
    onDeadlineChange: (Long?) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onProgressChange: (Float) -> Unit,
    onIsPublicChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    // 保存成功后导航返回
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Task" else "Push New Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack)
                    }
                }
            )
        }
    ) {
        Column {
            // 标题输入
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Title *") },
                isError = uiState.titleError != null
            )
            
            // 描述输入
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") }
            )
            
            // 截止时间选择
            DateTimePickerField(
                label = "Deadline",
                selectedTimestamp = uiState.deadline,
                onTimestampSelected = onDeadlineChange
            )
            
            // 开始时间选择
            DateTimePickerField(
                label = "Start Time",
                selectedTimestamp = uiState.startTime,
                onTimestampSelected = onStartTimeChange
            )
            
            // 优先级选择
            PrioritySelector(
                selectedPriority = uiState.priority,
                onPrioritySelected = onPriorityChange
            )
            
            // 进度滑块（编辑模式）
            if (isEditMode) {
                Text("Progress: ${(uiState.progress * 100).toInt()}%")
                Slider(
                    value = uiState.progress,
                    onValueChange = onProgressChange
                )
            }
            
            // 公开开关
            Row {
                Column {
                    Text("Public Task")
                    Text("Show on your public status page")
                }
                Switch(
                    checked = uiState.isPublic,
                    onCheckedChange = onIsPublicChange
                )
            }
            
            // 保存按钮
            Button(onClick = onSave) {
                Text(if (isEditMode) "Update Task" else "Push Task")
            }
        }
    }
}
```

### ArchiveScreen.kt

**路径:** `ui/screen/ArchiveScreen.kt`

归档页面 - 已完成任务列表。

```kotlin
@Composable
fun ArchiveScreen(
    uiState: ArchiveUiState,
    onDeleteTask: (String) -> Unit,
    onClearAll: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    var showClearAllDialog by remember { mutableStateOf(false) }
    
    // 确认对话框
    if (showClearAllDialog) {
        AlertDialog(
            title = { Text("Clear Archive") },
            text = { Text("Delete all archived tasks?") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearAllDialog = false
                }) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archive") },
                actions = {
                    if (uiState.archivedTasks.isNotEmpty()) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(Icons.Default.DeleteSweep)
                        }
                    }
                }
            )
        }
    ) {
        LazyColumn {
            items(uiState.archivedTasks, key = { it.id }) { task ->
                ArchivedTaskCard(
                    task = task,
                    onDelete = { onDeleteTask(task.id) }
                )
            }
        }
    }
}
```

### ProfileScreen.kt

**路径:** `ui/screen/ProfileScreen.kt`

个人中心 - 统计数据可视化。

```kotlin
@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh) }
                    IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings) }
                }
            )
        }
    ) {
        LazyColumn {
            // 用户信息卡片
            item { UserInfoCard() }
            
            // 统计概览 (今日/本周/活跃)
            item { StatsOverviewCard(statistics = uiState.statistics) }
            
            // 周活动柱状图
            item { WeeklyChartCard(weeklyData = uiState.statistics.weeklyData) }
            
            // 完成率环形图
            item { CompletionRateCard(completionRate = uiState.statistics.completionRate) }
        }
    }
}
```

### SettingsScreen.kt

**路径:** `ui/screen/SettingsScreen.kt`

设置页面 - 账户与同步配置。

```kotlin
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onSyncNow: () -> Unit,
    onAutoSyncToggle: (Boolean) -> Unit,
    onWifiOnlyToggle: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack)
                    }
                }
            )
        }
    ) {
        LazyColumn {
            // 账户部分
            item { AccountCard(...) }
            
            // 同步部分
            item {
                SyncCard(
                    isLoggedIn = uiState.isLoggedIn,
                    lastSyncTime = uiState.lastSyncTime,
                    isSyncing = uiState.isSyncing,
                    autoSyncEnabled = uiState.autoSyncEnabled,
                    syncOnWifiOnly = uiState.syncOnWifiOnly,
                    onSyncNow = onSyncNow,
                    onAutoSyncToggle = onAutoSyncToggle,
                    onWifiOnlyToggle = onWifiOnlyToggle
                )
            }
            
            // 关于部分
            item { AboutCard() }
        }
    }
}
```

---

## UI 设计模式

### 状态驱动 UI

```kotlin
// ViewModel 暴露 StateFlow
val uiState: StateFlow<SomeUiState>

// Composable 收集状态
@Composable
fun SomeScreen(viewModel: SomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 根据状态渲染 UI
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(uiState.error)
        else -> Content(uiState.data)
    }
}
```

### 错误处理

```kotlin
LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let { message ->
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }
}
```

### 导航触发

```kotlin
// 保存成功后导航
LaunchedEffect(uiState.isSaved) {
    if (uiState.isSaved) {
        onNavigateBack()
    }
}
```

---

## 相关文件

- `ui/theme/Theme.kt`, `Color.kt`, `Type.kt` - 主题
- `ui/components/TaskItem.kt` - 任务卡片
- `ui/components/TimelineView.kt` - 时间轴
- `ui/components/TemplateSelector.kt` - 模板选择器
- `ui/screen/*.kt` - 各页面
