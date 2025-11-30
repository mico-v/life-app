# StackDo (Life App) - 中文说明

> **"Push to Start, Pop to Finish."**  
> **"入栈开始，出栈完成。"**

[English README](README.md)

一款基于 **Push/Pop 堆栈思维** 的任务管理应用 - 将日常任务视为计算机栈结构。

## ✨ 功能特性

### 核心功能
- **Push (入栈)** - 快速创建任务，支持标题、截止时间和优先级
- **Pop (出栈)** - 右滑完成任务，满足感爆棚的手势交互
- **队列视图** - 按截止时间排序的任务列表
- **时间轴视图** - 基于时间流的任务可视化

### 高级功能
- **归档** - 查看已完成任务历史，支持搜索和标签筛选
- **模板** - 预设任务模板快速开始（工作、学习、健身等）
- **统计** - 追踪完成率、每日/每周进度
- **桌面组件** - 主屏幕小组件显示当前任务
- **通知提醒** - DDL 提醒和每日任务摘要
- **标签系统** - 使用自定义标签组织任务
- **主题切换** - 支持浅色/深色/跟随系统主题
- **多语言** - 支持中英文界面

### 云同步
- **服务器同步** - 基于密码认证的 RESTful API
- **公开状态** - 通过 Web 仪表盘分享忙碌/空闲状态
- **隐私控制** - 选择哪些任务公开显示

## 🛠 技术栈

| 类别 | 技术 |
|------|------|
| **UI** | Jetpack Compose (Material 3) |
| **架构** | MVVM + Repository 模式 |
| **数据库** | Room |
| **网络** | Retrofit + OkHttp |
| **后台任务** | WorkManager |
| **桌面组件** | Glance |
| **语言** | Kotlin 2.0 |
| **最低 SDK** | Android 8.0 (API 26) |
| **目标 SDK** | Android 16 (API 35) |

## 📱 截图

*即将推出*

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17

### 构建

```bash
# 克隆仓库
git clone https://github.com/mico-v/life-app.git

# 进入项目目录
cd life-app

# 构建 Debug APK
./gradlew assembleDebug

# 运行测试
./gradlew test
```

### 安装
```bash
# 安装到已连接的设备
./gradlew installDebug
```

## 📖 文档

详细的项目文档请参阅：

- **[项目摘要](app-design/PROJECT_SUMMARY.md)** - 完整项目概览（推荐首先阅读）
- **[应用入口与导航](app-design/docs/01-app-entry.md)** - 应用程序入口点
- **[数据层](app-design/docs/02-data-layer.md)** - Room 数据库和实体
- **[网络层](app-design/docs/03-network-layer.md)** - API 和同步
- **[ViewModel 层](app-design/docs/04-viewmodel-layer.md)** - 状态管理
- **[UI 层](app-design/docs/05-ui-layer.md)** - Compose UI 组件
- **[后台任务](app-design/docs/06-workers.md)** - WorkManager 后台任务
- **[桌面组件](app-design/docs/07-widget.md)** - 主屏幕小组件
- **[服务端部署](Server.md)** - 服务器部署指南

## 🌐 服务端

服务端位于 `/Server/` 目录，提供数据同步和 Web 仪表盘功能。

### 快速部署

```bash
cd Server
npm install
cp .env.example .env
# 编辑 .env 设置 SERVER_PASSWORD
npm start
```

详细部署指南请参阅 [Server.md](Server.md)。

## 📁 项目结构

```
app/src/main/java/com/example/android16demo/
├── LifeApp.kt              # Application 类
├── MainActivity.kt         # 主 Activity 和导航
├── data/                   # 数据层
│   ├── entity/             # Room 实体
│   ├── dao/                # 数据访问对象
│   ├── repository/         # 仓库
│   └── sync/               # 同步偏好设置
├── network/                # 网络层
│   ├── api/                # Retrofit API
│   └── model/              # DTO 数据传输对象
├── ui/                     # UI 层
│   ├── components/         # 可复用的 Composable
│   ├── screen/             # 页面 Composable
│   └── theme/              # Material 3 主题
├── viewmodel/              # ViewModel
├── widget/                 # Glance 桌面组件
└── worker/                 # WorkManager Worker
```

## 🎯 核心理念

将生活中的任务视为计算机的栈（Stack）或队列（Queue）：

```
┌─────────────────────────────┐
│       任务栈 (Stack)         │
├─────────────────────────────┤
│   → Push 入栈 (创建任务)     │
│   ← Pop 出栈 (完成任务)      │
│                             │
│   ┌───────────────────┐     │
│   │ 任务 3 (栈顶)     │ ←   │
│   ├───────────────────┤     │
│   │ 任务 2            │     │
│   ├───────────────────┤     │
│   │ 任务 1            │     │
│   └───────────────────┘     │
└─────────────────────────────┘
```

## 🔄 交互流程

### Push (创建任务)
1. 点击悬浮按钮 (FAB)
2. 填写任务信息（标题、截止时间、优先级）
3. 点击 "Push Task" 保存

### Pop (完成任务)
1. 在任务卡片上向右滑动
2. 绿色背景和对勾图标确认
3. 任务自动移至归档

### Delete (删除任务)
1. 在任务卡片上向左滑动
2. 红色背景和删除图标确认
3. 任务永久删除

## 🤝 贡献

欢迎贡献！请随时提交 Pull Request。

## 📄 许可证

本项目使用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件。

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)

---

*使用 Kotlin 和 Jetpack Compose 用 ❤️ 构建*
