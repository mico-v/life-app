# Life App（中文）

这是一个“个人状态 + 帖子流”项目，重点不是任务管理，而是公开展示“我此刻在做什么”。

- 网页公开页：主状态、来源状态、公开帖子
- 网页发布页：发布状态、发布/删除帖子
- Android App：`状态 / 资料 / 发布` 三个主界面
- 后端：Cloudflare Worker + D1

[English README](README.md)

## 当前功能

### Web（`Server/public`）
- ` / ` 公开展示：
  - 主状态（manual 优先）
  - 来源状态列表（只展示未过期状态）
  - 公开帖子流
- `/?view=publish`（或 `/publish`）发布页：
  - 发布手动状态（支持 TTL）
  - 发布帖子（`content/tags/location`）
  - 查看并删除我的帖子
- 文案、标题、外链通过 `Server/public/config.json` 配置

### Worker（`Server/worker`）
- 鉴权 API（`x-client-token` + `x-server-password`）
  - `POST /api/v1/status/events`
  - `GET /api/v1/status`
  - `POST /api/v1/posts`
  - `PUT /api/v1/posts/:postId`
  - `DELETE /api/v1/posts/:postId`
  - `GET /api/v1/posts`
- 公开 API
  - `GET /api/v1/public/feed`
- 兼容保留（旧任务同步链路）
  - `POST /api/v1/sync`
  - `GET /api/v1/tasks`

### Android（`app`）
- `Status`：读取公开 feed
- `Profile`：管理本地资料配置
- `Publish`：发布状态、发布帖子、管理帖子

## 技术栈

- Android: Kotlin + Jetpack Compose + Material 3
- 网络: Retrofit + OkHttp
- 后端: Cloudflare Worker (Hono) + D1
- CI: Worker 部署 + Android 签名发布

## 快速开始

### 1) 启动 Worker 本地环境

```bash
cd Server/worker
corepack enable
pnpm install
pnpm run db:migrate:local
pnpm run dev
```

### 2) 访问页面

- 公开页：`http://127.0.0.1:8787/`
- 发布页：`http://127.0.0.1:8787/?view=publish`

### 3) Android 构建（可选）

```bash
./gradlew :app:assembleDebug
```

## Android 签名一致性（GitHub Actions）

```bash
chmod +x scripts/android_signing_setup.sh
./scripts/android_signing_setup.sh
```

在仓库 Secrets 中配置：
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

工作流：
- 推送 `main`：仅编译校验
- 推送 `v*` tag：签名构建 release APK 并发布
- 手动触发：执行签名构建

## 文档索引

- `Server/worker/README.md`
- `app-design/PROJECT_SUMMARY.md`
- `app-design/docs/01-app-entry.md` 到 `app-design/docs/07-widget.md`

## 许可证

MIT
