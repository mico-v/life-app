# Life App (Push/Pop 任务管理器) - 设计总览

## 1. 产品理念
**"Push to Start, Pop to Finish."**
将生活中的任务视为计算机的栈（Stack）或队列（Queue）。
- **Push (入栈)**: 快速捕捉任务，开始行动。
- **Queue (队列)**: 在时间轴上可视化工作负载。
- **Pop (出栈)**: 完成任务，获得满足感并归档。
- **Broadcast (广播)**: 向世界分享你的忙碌/空闲状态。

## 2. 核心功能模块

### A. 移动端 App (Android)
1.  **The Queue (主页)**
    - **时间轴视图**: 按时间/截止日期排序的任务可视化。
    - **状态指示**: 通过颜色区分紧急程度或进度。
    - **快捷操作**: 滑动 "Pop" (完成)，点击展开。

2.  **Push (创建任务)**
    - **快速入口**: 仅需标题和可选的 DDL。
    - **详细模式**: 内容描述、时间段 (Start - End)、引用资料。
    - **模板系统**: "工作"、"学习"、"健身" 等预设模板。

3.  **Pop (完成与归档)**
    - **交互反馈**: 完成任务时的满足感动画。
    - **归档**: 查看已 "Pop" 的任务历史。
    - **进度追踪**: 活跃任务的 0-100% 进度滑块。

4.  **个人中心**
    - **统计**: "本周 Pop 任务数"、"专注时长"。
    - **个人资料**: 用户信息管理。

5.  **服务器同步 (Broadcast)**
    - **状态同步**: 将本地状态推送到服务器。
    - **隐私控制**: 选择哪些任务是 "公开" 或 "私密" 的。

### B. Web 状态看板 (Server)
1.  **公开主页**: `life-app.com/u/username`
2.  **状态指示器**: BUSY (忙碌中) / FREE (空闲)。
3.  **时间轴视图**: 只读的公开任务视图。

## 3. 技术架构

- **Android 客户端**:
    - **UI**: Jetpack Compose (Material 3)
    - **架构**: MVVM (Model-View-ViewModel) + Clean Architecture
    - **本地数据**: Room Database (单一数据源)
    - **网络**: Retrofit
    - **后台任务**: WorkManager

- **后端 (Server)**:
    - **技术栈**: Python FastAPI 或 Node.js Express (轻量级)
    - **数据库**: SQLite / PostgreSQL
    - **前端**: React 或 静态 HTML/JS

## 4. 开发计划与文档导航

本项目将分为四个阶段进行开发，每个阶段都有详细的设计文档：

- **[阶段 1: MVP 基础功能](phase1-mvp.md)**
    - 目标：实现本地的 Push/Pop 核心循环，数据库搭建，基础列表视图。
    - 重点：Room Entity 设计, CRUD 操作, 基础 Compose UI。

- **[阶段 2: 视觉与交互进阶](phase2-ui-ux.md)**
    - 目标：实现时间轴视图 (Timeline)，手势交互，DDL 提醒。
    - 重点：自定义 Layout, 动画效果, Notification Manager。

- **[阶段 3: 服务器与 Web 同步](phase3-server.md)**
    - 目标：搭建后端服务，实现数据同步，Web 端状态展示。
    - 重点：API 设计, 数据同步策略, Web 前端开发。

- **[阶段 4: 完善与优化](phase4-polish.md)**
    - 目标：模板系统，个人中心统计，UI 细节打磨。
    - 重点：数据统计逻辑, 模板管理, 性能优化。
