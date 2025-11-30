# StackDo (Life App) - 项目摘要文档

> **"Push to Start, Pop to Finish."** / **"入栈开始，出栈完成"**

## 📂 项目概览 (Project Overview)

### 基本信息
- **项目名称:** StackDo (Life App)
- **包名:** `com.example.android16demo`
- **最低SDK:** Android 8.0 (API 26)
- **目标SDK:** Android 16 (API 35)
- **开发语言:** Kotlin 2.0.21

### 核心理念
基于 **"Push/Pop" 堆栈思维** 的任务管理应用。将生活中的任务视为计算机的栈（Stack）或队列（Queue）：
- **Push (入栈):** 快速捕捉任务，开始行动
- **Queue (队列):** 在时间轴上可视化工作负载
- **Pop (出栈):** 完成任务，获得满足感并归档
- **Broadcast (广播):** 向服务器同步你的忙碌/空闲状态

### 当前状态
✅ **核心功能已完成：**
- 完整的 Push/Pop 任务生命周期
- 列表视图和时间轴视图双模式
- SwipeToDismiss 手势交互
- Room 本地数据库持久化
- WorkManager 后台任务与通知
- Retrofit 网络层（服务器同步准备就绪）
- 任务模板系统
- 统计面板
- 桌面小组件 (Glance)
- **Profile 页面** - 用户信息、自定义格言、状态、成就系统
- **Settings 页面** - 服务器配置、Push模板、账户管理
- **页面切换动画** - 流畅的导航过渡动画
- **时间轴优化** - 显示时间基线和当前时间指示器

✅ **v2.0 新增功能：**
- **标签系统 (Tags)** - 任务支持自定义标签，主页和归档页支持按标签筛选
- **搜索功能** - 归档页面支持任务搜索
- **时间选择器** - 截止时间支持日期+时间选择（默认 24:00）
- **主题切换** - 支持浅色/深色/跟随系统三种模式
- **中文支持** - 完整的中文界面本地化
- **新同步机制** - 客户端自动生成 Token，服务端密码验证
- **自适应图标** - Material Design 风格的栈式图标
- **服务端** - Node.js + Express 服务器 + Material Design 3 Web 仪表盘

---

## 🏗️ 技术栈与架构 (Tech Stack & Architecture)

### 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **UI 框架** | Jetpack Compose | BOM 2024.06.00 |
| **设计系统** | Material 3 | 动态取色 (Dynamic Color) |
| **数据库** | Room | 2.6.1 |
| **网络** | Retrofit + OkHttp | 2.9.0 / 4.12.0 |
| **后台任务** | WorkManager | 2.9.0 |
| **桌面组件** | Glance | 1.1.0 |
| **安全存储** | EncryptedSharedPreferences | 1.1.0-alpha06 |
| **协程** | Kotlin Coroutines | 1.7.3 |
| **导航** | Navigation Compose | 2.8.4 |

### 架构模式
```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │   Screens   │  │ Components  │  │   Theme     │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
├─────────────────────────────────────────────────────────────┤
│                     ViewModel Layer                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │HomeViewModel│  │ArchiveVM   │  │TaskDetailVM │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
├─────────────────────────────────────────────────────────────┤
│                    Repository Layer                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │TaskRepo     │  │TemplateRepo│  │SyncRepo     │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │  Room DB    │  │ Retrofit API│  │ Preferences │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

**架构遵循:**
- **MVVM** (Model-View-ViewModel)
- **单向数据流** (Unidirectional Data Flow)
- **Repository 模式** 隔离数据源
- **StateFlow** 响应式状态管理

---

## 📱 界面功能设计 (UI & Features)

### 导航结构
```
MainActivity
    └── NavHost (with animations)
        ├── Queue Screen (主页 - 列表/时间轴视图)
        │   ├── TaskItem (滑动完成/删除)
        │   └── FAB (添加任务)
        ├── Archive Screen (归档 - 已完成任务)
        ├── Profile Screen (个人中心)
        │   ├── 用户信息卡片 (可编辑名称/格言)
        │   ├── 状态显示
        │   ├── 统计数据面板
        │   ├── 成就进度
        │   └── 设置入口
        ├── Settings Screen (设置)
        │   ├── 账户管理 (登录/登出)
        │   ├── 服务器配置 (域名/IP:端口)
        │   ├── Push通知模板
        │   └── 同步设置
        └── Task Detail Screen (任务详情 - 创建/编辑)
```

### 导航动画
| 动画类型 | 使用场景 | 效果 |
|---------|---------|------|
| fadeIn/fadeOut | 底部导航切换 | 淡入淡出 (300ms) |
| slideLeft/Right | Settings 页面 | 左右滑动进入/退出 |
| slideUp/Down | Task Detail | 从下方弹出/收起 |

### 核心界面

| 界面 | 功能 | 文件位置 |
|------|------|----------|
| **Task Queue** | 主页，显示活跃任务队列 | `ui/screen/TaskQueueScreen.kt` |
| **Timeline** | 时间轴视图，按时间分组，显示时间基线 | `ui/screen/TimelineScreen.kt` |
| **Archive** | 已完成任务归档 | `ui/screen/ArchiveScreen.kt` |
| **Task Detail** | 创建/编辑任务表单 | `ui/screen/TaskDetailScreen.kt` |
| **Profile** | 用户信息、格言、状态、统计可视化、成就 | `ui/screen/ProfileScreen.kt` |
| **Settings** | 服务器配置、Push模板、账户管理 | `ui/screen/SettingsScreen.kt` |

### Profile 页面功能
- **用户信息卡片**: 可编辑显示名称和个人格言
- **状态指示器**: 显示 Available/Busy/Away 状态
- **统计概览**: 今日完成、本周完成、活跃任务数
- **周活动图表**: 过去7天的任务完成柱状图（动画）
- **完成率环形图**: 按时完成率可视化（动画）
- **成就系统**: First Steps, Getting Started, Productive, Master, Legend

### Settings 页面功能
- **账户管理**: 登录/登出功能
- **服务器配置**: 远端推送目标服务器设置（域名/IP:端口）
- **Push 模板**: 默认/紧急/静默/摘要 四种通知模板
- **同步设置**: 手动同步、自动同步、仅Wi-Fi同步

### 核心交互
- **Push (创建任务):** 点击 FAB → TaskDetailScreen → 填写表单 → 保存
- **Pop (完成任务):** 右滑 TaskItem → SwipeToDismiss → 绿色背景确认 → 任务移至归档
- **Delete (删除任务):** 左滑 TaskItem → SwipeToDismiss → 红色背景确认 → 永久删除
- **视图切换:** TopAppBar 图标切换 LIST ↔ TIMELINE 视图

---

## 🗃️ 数据模型 (Data Models)

### Task Entity
```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,     // UUID
    val title: String,               // 任务标题 (必填)
    val description: String?,        // 描述 (可选)
    val createdAt: Long,             // 创建时间
    val startTime: Long?,            // 计划开始时间
    val deadline: Long?,             // 截止时间 (DDL)
    val isCompleted: Boolean,        // 是否已完成
    val completedAt: Long?,          // 完成时间
    val progress: Float,             // 进度 0.0 - 1.0
    val priority: Int,               // 优先级 1/2/3
    val isPublic: Boolean,           // 是否公开同步
    val tags: String?                // 标签（逗号分隔）
)
```

### TaskTemplate Entity
```kotlin
@Entity(tableName = "task_templates")
data class TaskTemplate(
    @PrimaryKey val id: String,
    val name: String,                // 模板名称
    val defaultTitle: String,        // 默认标题
    val defaultDescription: String?,
    val defaultDurationMinutes: Int?,
    val defaultPriority: Int,
    val iconName: String,            // 图标标识
    val colorHex: String,            // 颜色值
    val sortOrder: Int,
    val isBuiltIn: Boolean           // 内置/用户创建
)
```

---

## 📄 核心代码文件导航 (Critical File Locations)

> **AI Agent 阅读指南：** 请按以下顺序阅读文档和源代码，以便快速理解项目结构。

### 1️⃣ 应用入口
| 文件 | 说明 | 详细文档 |
|------|------|----------|
| `LifeApp.kt` | Application 类，提供单例依赖 | [docs/01-app-entry.md](docs/01-app-entry.md) |
| `MainActivity.kt` | 主 Activity，导航宿主 | [docs/01-app-entry.md](docs/01-app-entry.md) |

### 2️⃣ 数据层 (Data Layer)
| 文件 | 说明 | 详细文档 |
|------|------|----------|
| `data/entity/Task.kt` | 任务实体定义 | [docs/02-data-layer.md](docs/02-data-layer.md) |
| `data/entity/TaskTemplate.kt` | 任务模板实体 | [docs/02-data-layer.md](docs/02-data-layer.md) |
| `data/dao/TaskDao.kt` | 任务数据访问对象 | [docs/02-data-layer.md](docs/02-data-layer.md) |
| `data/dao/TaskTemplateDao.kt` | 模板数据访问对象 | [docs/02-data-layer.md](docs/02-data-layer.md) |
| `data/AppDatabase.kt` | Room 数据库定义 | [docs/02-data-layer.md](docs/02-data-layer.md) |
| `data/repository/TaskRepository.kt` | 任务仓库 | [docs/02-data-layer.md](docs/02-data-layer.md) |
| `data/repository/TemplateRepository.kt` | 模板仓库 | [docs/02-data-layer.md](docs/02-data-layer.md) |

### 3️⃣ 网络层 (Network Layer)
| 文件 | 说明 | 详细文档 |
|------|------|----------|
| `network/api/LifeAppApi.kt` | Retrofit API 接口定义 | [docs/03-network-layer.md](docs/03-network-layer.md) |
| `network/model/ApiModels.kt` | DTO 数据传输对象 | [docs/03-network-layer.md](docs/03-network-layer.md) |
| `network/RetrofitClient.kt` | Retrofit 客户端配置 | [docs/03-network-layer.md](docs/03-network-layer.md) |
| `data/sync/SyncRepository.kt` | 同步仓库逻辑 | [docs/03-network-layer.md](docs/03-network-layer.md) |
| `data/sync/SyncPreferences.kt` | 加密存储认证信息 | [docs/03-network-layer.md](docs/03-network-layer.md) |

### 4️⃣ ViewModel 层
| 文件 | 说明 | 详细文档 |
|------|------|----------|
| `viewmodel/HomeViewModel.kt` | 主页视图模型 | [docs/04-viewmodel-layer.md](docs/04-viewmodel-layer.md) |
| `viewmodel/ArchiveViewModel.kt` | 归档视图模型 | [docs/04-viewmodel-layer.md](docs/04-viewmodel-layer.md) |
| `viewmodel/TaskDetailViewModel.kt` | 任务详情视图模型 | [docs/04-viewmodel-layer.md](docs/04-viewmodel-layer.md) |
| `viewmodel/ProfileViewModel.kt` | 个人中心视图模型（统计、用户配置） | [docs/04-viewmodel-layer.md](docs/04-viewmodel-layer.md) |
| `viewmodel/SettingsViewModel.kt` | 设置视图模型（服务器、Push模板） | [docs/04-viewmodel-layer.md](docs/04-viewmodel-layer.md) |
| `viewmodel/ViewModelFactory.kt` | ViewModel 工厂类 | [docs/04-viewmodel-layer.md](docs/04-viewmodel-layer.md) |

### 5️⃣ UI 层 (Compose)
| 文件 | 说明 | 详细文档 |
|------|------|----------|
| `ui/theme/Theme.kt` | Material 3 主题配置 | [docs/05-ui-layer.md](docs/05-ui-layer.md) |
| `ui/theme/Color.kt` | 颜色定义 | [docs/05-ui-layer.md](docs/05-ui-layer.md) |
| `ui/theme/Type.kt` | 字体样式定义 | [docs/05-ui-layer.md](docs/05-ui-layer.md) |
| `ui/components/TaskItem.kt` | 任务卡片组件 | [docs/05-ui-layer.md](docs/05-ui-layer.md) |
| `ui/components/TimelineView.kt` | 时间轴组件 | [docs/05-ui-layer.md](docs/05-ui-layer.md) |
| `ui/components/TemplateSelector.kt` | 模板选择器组件 | [docs/05-ui-layer.md](docs/05-ui-layer.md) |
| `ui/screen/*.kt` | 各页面 Screen Composable | [docs/05-ui-layer.md](docs/05-ui-layer.md) |

### 6️⃣ 后台任务 (Workers)
| 文件 | 说明 | 详细文档 |
|------|------|----------|
| `worker/DeadlineReminderWorker.kt` | DDL 提醒通知 | [docs/06-workers.md](docs/06-workers.md) |
| `worker/DailySummaryWorker.kt` | 每日任务摘要通知 | [docs/06-workers.md](docs/06-workers.md) |
| `worker/SyncWorker.kt` | 后台数据同步 | [docs/06-workers.md](docs/06-workers.md) |

### 7️⃣ 桌面组件 (Widget)
| 文件 | 说明 | 详细文档 |
|------|------|----------|
| `widget/LifeAppWidget.kt` | Glance 桌面小组件 | [docs/07-widget.md](docs/07-widget.md) |
| `res/xml/life_app_widget_info.xml` | 小组件配置 | [docs/07-widget.md](docs/07-widget.md) |

---

## 🔧 构建与测试 (Build & Test)

### 构建命令
```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 运行单元测试
./gradlew test

# 运行 UI 测试
./gradlew connectedAndroidTest

# 代码检查
./gradlew lint
```

### 依赖管理
所有依赖定义在 `gradle/libs.versions.toml` 中。添加新依赖时：
1. 在 `[versions]` 块定义版本号
2. 在 `[libraries]` 块定义依赖
3. 在 `app/build.gradle.kts` 中使用 `libs.xxx` 引用

---

## 📋 开发约定 (Development Conventions)

### 命名规范
- **Composable 函数:** PascalCase (如 `TaskQueueScreen`)
- **ViewModel:** 以 `ViewModel` 结尾 (如 `HomeViewModel`)
- **Repository:** 以 `Repository` 结尾 (如 `TaskRepository`)
- **Entity:** 数据类名对应表名 (如 `Task` → `tasks`)

### 状态管理
- 使用 `StateFlow` 暴露 UI 状态
- UI State 定义为不可变 `data class`
- 通过 `copy()` 方法更新状态

### 导航
- 路由定义在 `sealed class Screen`
- 使用 `NavHost` 进行页面导航
- 参数传递使用 `navArgument`

---

## 🚀 快速开始 (Quick Start for AI Agent)

1. **理解项目结构:** 先阅读本文档
2. **数据层:** 阅读 [docs/02-data-layer.md](docs/02-data-layer.md)
3. **UI 层:** 阅读 [docs/05-ui-layer.md](docs/05-ui-layer.md)
4. **修改前:** 运行 `./gradlew assembleDebug` 确保项目可编译
5. **修改后:** 运行测试并确保通过

---

## 📁 目录结构

```
app/src/main/java/com/example/android16demo/
├── LifeApp.kt                 # Application 入口
├── MainActivity.kt            # 主 Activity
├── data/
│   ├── AppDatabase.kt         # Room 数据库
│   ├── dao/
│   │   ├── TaskDao.kt         # 任务 DAO
│   │   └── TaskTemplateDao.kt # 模板 DAO
│   ├── entity/
│   │   ├── Task.kt            # 任务实体
│   │   └── TaskTemplate.kt    # 模板实体
│   ├── repository/
│   │   ├── TaskRepository.kt  # 任务仓库
│   │   └── TemplateRepository.kt
│   └── sync/
│       ├── SyncPreferences.kt # 同步偏好设置
│       └── SyncRepository.kt  # 同步仓库
├── network/
│   ├── RetrofitClient.kt      # 网络客户端
│   ├── api/
│   │   └── LifeAppApi.kt      # API 接口
│   └── model/
│       └── ApiModels.kt       # 网络数据模型
├── ui/
│   ├── components/
│   │   ├── TaskItem.kt        # 任务卡片
│   │   ├── TemplateSelector.kt
│   │   └── TimelineView.kt    # 时间轴视图
│   ├── screen/
│   │   ├── ArchiveScreen.kt
│   │   ├── ProfileScreen.kt
│   │   ├── SettingsScreen.kt
│   │   ├── TaskDetailScreen.kt
│   │   ├── TaskQueueScreen.kt
│   │   └── TimelineScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/
│   ├── ArchiveViewModel.kt
│   ├── HomeViewModel.kt
│   ├── ProfileViewModel.kt    # 用户配置、统计数据
│   ├── SettingsViewModel.kt   # 服务器配置、Push模板
│   ├── TaskDetailViewModel.kt
│   └── ViewModelFactory.kt
├── widget/
│   └── LifeAppWidget.kt       # 桌面小组件
└── worker/
    ├── DailySummaryWorker.kt  # 每日摘要
    ├── DeadlineReminderWorker.kt # DDL 提醒
    └── SyncWorker.kt          # 后台同步
```

---

## 🌐 服务端 (Server)

服务端位于 `/Server/` 目录，提供数据同步和 Web 仪表盘功能。

### 技术栈
- **运行时:** Node.js 18+
- **框架:** Express.js
- **数据库:** NeDB (嵌入式)
- **前端:** 原生 HTML/CSS/JS + Material Design 3

### 功能
- 任务数据同步 API
- 用户资料管理
- 时间轴数据查询
- Material Design 3 风格 Web 仪表盘
- 基于密码的简单认证机制

### 部署
详见 [Server.md](/Server.md) 部署指南。

---

## 📚 文档索引

| 文档 | 内容 |
|------|------|
| [docs/01-app-entry.md](docs/01-app-entry.md) | 应用入口与导航 |
| [docs/02-data-layer.md](docs/02-data-layer.md) | 数据层详解 |
| [docs/03-network-layer.md](docs/03-network-layer.md) | 网络层详解 |
| [docs/04-viewmodel-layer.md](docs/04-viewmodel-layer.md) | ViewModel 层详解 |
| [docs/05-ui-layer.md](docs/05-ui-layer.md) | UI 层详解 |
| [docs/06-workers.md](docs/06-workers.md) | 后台任务详解 |
| [docs/07-widget.md](docs/07-widget.md) | 桌面组件详解 |
| [/Server.md](/Server.md) | 服务端部署指南 |

---

*文档生成时间: 2024年*
