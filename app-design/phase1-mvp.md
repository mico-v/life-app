# 阶段 1: MVP 基础功能 (Minimum Viable Product)

## 目标
构建应用的骨架，实现最核心的 "Push" (创建) 和 "Pop" (完成) 循环。此阶段不涉及复杂的 UI 动画或服务器同步，专注于数据的准确性和基础交互。

## 1. 数据层设计 (Room Database)

### 1.1 Entity: `Task`
```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startTime: Long? = null, // 计划开始时间
    val deadline: Long? = null,  // 截止时间
    val isCompleted: Boolean = false, // 是否已 Pop
    val completedAt: Long? = null,
    val progress: Float = 0f, // 0.0 - 1.0
    val priority: Int = 1 // 1: Low, 2: Medium, 3: High
)
```

### 1.2 DAO: `TaskDao`
- `getAllTasks(): Flow<List<Task>>` (按时间排序)
- `getActiveTasks(): Flow<List<Task>>` (未完成的任务)
- `getArchivedTasks(): Flow<List<Task>>` (已完成的任务)
- `insertTask(task: Task)`
- `updateTask(task: Task)`
- `deleteTask(task: Task)`
- `getTaskById(id: String): Flow<Task?>`

### 1.3 Repository
- `TaskRepository`: 封装 DAO 操作，为 ViewModel 提供干净的 API。

## 2. 架构与状态管理 (MVVM)

### 2.1 ViewModels
- **`HomeViewModel`**:
    - `uiState`: 包含 `activeTasks` 列表, `isLoading`, `errorMessage`。
    - `actions`: `popTask(id)`, `deleteTask(id)`.
- **`TaskDetailViewModel`**:
    - 用于创建和编辑任务。
    - 处理表单验证 (标题不能为空)。

## 3. UI 界面 (Jetpack Compose)

### 3.1 首页 (Home Screen)
- **布局**: `Scaffold`
- **列表**: `LazyColumn` 展示 `TaskItem`。
- **TaskItem 组件**:
    - 显示标题、截止时间。
    - 简单的 Checkbox 或 点击事件来模拟 "Pop"。
- **FAB**: 悬浮按钮 "+" 跳转到创建页面。

### 3.2 创建/编辑页 (Task Screen)
- **表单**:
    - `OutlinedTextField` (标题, 描述)。
    - `DatePicker` / `TimePicker` (开始时间, DDL)。
    - `Slider` (进度，初始为 0)。
- **保存按钮**: 提交数据到 ViewModel。

## 4. 开发步骤 Prompt 指引

1.  **环境搭建**: "请帮我配置 Room 数据库依赖，并创建 `Task` Entity 和 `AppDatabase`。"
2.  **数据层实现**: "请实现 `TaskDao` 和 `TaskRepository`，支持基本的增删改查和 Flow 观察。"
3.  **ViewModel 实现**: "请创建 `HomeViewModel`，从 Repository 获取任务列表并暴露 `StateFlow` 给 UI。"
4.  **UI 实现**: "请使用 Jetpack Compose 创建一个简单的任务列表界面，包含一个 FAB 用于添加任务。"
