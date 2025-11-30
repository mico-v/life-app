# 01 - 应用入口与导航 (App Entry & Navigation)

## 概述

本文档描述 StackDo 应用的入口点和导航系统。

---

## LifeApp.kt

**路径:** `com/example/android16demo/LifeApp.kt`

### 功能
`LifeApp` 是 `Application` 子类，负责：
1. 提供全局单例依赖
2. 初始化数据库和仓库

### 关键代码

```kotlin
class LifeApp : Application() {
    
    // 懒加载数据库实例
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
    
    // 懒加载 TaskRepository
    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }
    
    // 懒加载 TemplateRepository
    val templateRepository: TemplateRepository by lazy {
        TemplateRepository(database.taskTemplateDao())
    }
}
```

### 依赖注入模式
项目使用 **手动依赖注入** 而非 Hilt/Dagger：
- `LifeApp` 作为依赖容器
- 通过 `context.applicationContext as LifeApp` 获取依赖
- `ViewModelFactory` 负责创建带依赖的 ViewModel

### 在 AndroidManifest.xml 中注册
```xml
<application
    android:name=".LifeApp"
    ...
```

---

## MainActivity.kt

**路径:** `com/example/android16demo/MainActivity.kt`

### 功能
主 Activity，承载整个 Compose UI 树和导航系统。

### 关键组成部分

#### 1. Screen 路由定义

```kotlin
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Queue : Screen("queue", "Queue", Icons.Filled.FormatListBulleted)
    data object Archive : Screen("archive", "Archive", Icons.Filled.Archive)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    data object TaskDetail : Screen("task/{taskId}", "Task") {
        fun createRoute(taskId: String?) = if (taskId != null) "task/$taskId" else "task/new"
    }
}
```

#### 2. 底部导航栏配置

```kotlin
val bottomNavScreens = listOf(Screen.Queue, Screen.Archive, Screen.Profile)
```

#### 3. 视图模式枚举

```kotlin
enum class ViewMode {
    LIST,      // 列表视图
    TIMELINE   // 时间轴视图
}
```

#### 4. 主 Composable - LifeAppMain()

```kotlin
@Composable
fun LifeAppMain() {
    val navController = rememberNavController()
    var viewMode by rememberSaveable { mutableStateOf(ViewMode.LIST) }
    
    Scaffold(
        topBar = { /* CenterAlignedTopAppBar */ },
        bottomBar = { /* NavigationBar with 3 items */ }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Queue.route
        ) {
            // 定义各个 composable 路由
        }
    }
}
```

### 导航流程图

```
┌─────────────────────────────────────────────────┐
│                 MainActivity                     │
│  ┌─────────────────────────────────────────┐    │
│  │            CenterAlignedTopAppBar        │    │
│  │  [Life App]              [切换视图图标]  │    │
│  └─────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────┐    │
│  │                                          │    │
│  │              NavHost 内容区               │    │
│  │                                          │    │
│  │   Queue / Archive / Profile / Detail    │    │
│  │                                          │    │
│  └─────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────┐    │
│  │           NavigationBar                  │    │
│  │   [Queue]    [Archive]    [Profile]     │    │
│  └─────────────────────────────────────────┘    │
└─────────────────────────────────────────────────┘
```

### ViewModel 创建模式

```kotlin
// 获取 Application 实例
val app = LocalContext.current.applicationContext as LifeApp

// 使用 ViewModelFactory 创建 ViewModel
val viewModel: HomeViewModel = viewModel(
    factory = ViewModelFactory(app.taskRepository)
)
```

### 导航操作

```kotlin
// 导航到任务详情（编辑）
navController.navigate(Screen.TaskDetail.createRoute(taskId))

// 导航到任务详情（新建）
navController.navigate(Screen.TaskDetail.createRoute(null))

// 返回上一页
navController.popBackStack()

// 底部导航切换
navController.navigate(screen.route) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

### 初始化流程

```
Application.onCreate()
       │
       ▼
MainActivity.onCreate()
       │
       ├── enableEdgeToEdge()
       │
       ├── DailySummaryWorker.scheduleDailySummary(this)
       │
       └── setContent {
               Android16DemoTheme {
                   LifeAppMain()
               }
           }
```

---

## 如何扩展

### 添加新页面

1. 在 `Screen` sealed class 中添加新路由：
```kotlin
data object NewScreen : Screen("new_screen", "New", Icons.Filled.Star)
```

2. 在 `NavHost` 中添加 composable：
```kotlin
composable(Screen.NewScreen.route) {
    NewScreenComposable(...)
}
```

3. 如果需要底部导航，添加到 `bottomNavScreens`：
```kotlin
val bottomNavScreens = listOf(Screen.Queue, Screen.Archive, Screen.Profile, Screen.NewScreen)
```

### 传递参数

```kotlin
// 定义带参数的路由
data object Detail : Screen("detail/{id}", "Detail") {
    fun createRoute(id: String) = "detail/$id"
}

// 在 NavHost 中声明参数
composable(
    route = Screen.Detail.route,
    arguments = listOf(
        navArgument("id") { type = NavType.StringType }
    )
) { backStackEntry ->
    val id = backStackEntry.arguments?.getString("id")
    // ...
}
```

---

## 相关文件

- `AndroidManifest.xml` - 应用配置
- `ui/theme/Theme.kt` - 主题配置
- `viewmodel/ViewModelFactory.kt` - ViewModel 工厂
