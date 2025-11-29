# 阶段 3: 服务器与 Web 同步 (Server & Web Sync)

## 目标
打破单机限制，实现数据的云端备份，并创建一个公开的 Web 页面展示用户的 "Busy/Free" 状态和公开任务。

## 1. 后端 API 设计 (RESTful)

### 1.1 技术栈建议
- **Language**: Python (FastAPI) 或 Node.js (Express/Koa)。
- **Database**: SQLite (开发期) -> PostgreSQL (生产期)。
- **Hosting**: Vercel / Render / 自建 VPS。

### 1.2 Endpoints
- `POST /api/v1/sync`: 接收客户端上传的 JSON 数据（全量或增量）。
    - Body: `{ userId: "...", tasks: [...] }`
- `GET /api/v1/u/{username}/status`: 获取指定用户的公开状态。
    - Response: `{ status: "BUSY", currentTask: "Coding", timeline: [...] }`

## 2. Android 客户端同步

### 2.1 网络层
- **Retrofit**: 定义 API 接口。
- **DTOs**: 定义用于网络传输的数据模型 (可能与 Room Entity 略有不同)。

### 2.2 同步策略
- **手动同步**: 设置页面的 "Sync Now" 按钮。
- **自动同步**:
    - 数据变更时 (Debounce 5秒)。
    - App 启动/后台运行时 (WorkManager)。
- **隐私过滤**: 只上传 `isPublic = true` 的任务到公开接口，但全量备份到私有存储。

## 3. Web 前端 (Status Board)

### 3.1 页面设计
- **URL**: `life-app.com/u/mico`
- **Header**: 用户头像，当前状态 (大字 BUSY/FREE)。
- **Body**: 当天的公开任务时间轴。
- **Footer**: "Powered by Life App".

### 3.2 实现
- 简单的 React 单页应用 (SPA) 或 静态 HTML + JS Fetch。
- 自动刷新 (每分钟轮询一次状态)。

## 4. 开发步骤 Prompt 指引

1.  **后端搭建**: "请用 Python FastAPI 写一个简单的后端，提供 `/sync` 和 `/status` 两个接口，使用 SQLite 存储。"
2.  **网络请求**: "请在 Android 项目中配置 Retrofit，定义与后端对应的 API 接口和数据模型。"
3.  **同步逻辑**: "请实现一个 `SyncRepository`，负责将本地 Room 数据库中的任务转换为 JSON 并上传到服务器。"
4.  **Web 页面**: "请写一个简单的 HTML/CSS/JS 页面，通过 fetch 请求 `/status` 接口并展示用户的时间轴。"
