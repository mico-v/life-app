# Life App

个人状态与帖子流项目，核心是发布和展示“我现在在做什么”。

- Web 公开页：展示主状态、来源状态列表、公开帖子流
- Web 发布页：发布手动状态、发布/删除帖子
- Android App：MD3 风格的 `Status / Profile / Publish` 三页
- Worker API：Cloudflare Worker + D1，支持状态时效（`expires_at`）

[中文说明](README_CN.md)

## 当前能力

### Web（`Server/public`）
- 公开页 `/`：
  - 主状态（manual 优先）
  - 来源状态列表（仅有效状态）
  - 最新公开帖子
- 发布页 `/?view=publish` 或 `/publish`：
  - 发布手动状态（可设置 TTL）
  - 发布帖子（content/tags/location）
  - 查看并删除自己的帖子
- 文案与外链来自 `Server/public/config.json`

### Worker（`Server/worker`）
- 鉴权接口（需要 `x-client-token` + `x-server-password`）
  - `POST /api/v1/status/events`
  - `GET /api/v1/status`
  - `POST /api/v1/posts`
  - `PUT /api/v1/posts/:postId`
  - `DELETE /api/v1/posts/:postId`
  - `GET /api/v1/posts`
- 公开接口
  - `GET /api/v1/public/feed`
- 遗留兼容（保留，不作为主功能）
  - `POST /api/v1/sync`
  - `GET /api/v1/tasks`

### Android（`app`）
- `Status`：查看公开 feed（主状态、来源状态、帖子）
- `Profile`：本地资料配置（如显示名/格言等）
- `Publish`：发布状态与帖子，管理已发布帖子

## 技术栈

- Android: Kotlin + Jetpack Compose + Material 3
- Network: Retrofit + OkHttp
- Backend: Cloudflare Worker (Hono) + D1
- CI:
  - `.github/workflows/worker_deploy.yml`
  - `.github/workflows/android_release.yml`

## 快速开始

### 1) 启动 Worker 本地开发

```bash
cd Server/worker
corepack enable
pnpm install
pnpm run db:migrate:local
pnpm run dev
```

### 2) 打开网页

- 公开页：`http://127.0.0.1:8787/`
- 发布页：`http://127.0.0.1:8787/?view=publish`

### 3) Android 构建（可选）

```bash
./gradlew :app:assembleDebug
```

## Android 签名发布（GitHub Actions）

固定使用同一 keystore，保证每次 release 签名一致。

```bash
chmod +x scripts/android_signing_setup.sh
./scripts/android_signing_setup.sh
```

GitHub Secrets:
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

工作流行为：
- `push main`：编译校验（`assembleDebug`）
- `tag v*`：签名构建并发布 release APK
- `workflow_dispatch`：手动触发签名构建

## 文档索引

- `Server/worker/README.md`：Worker 部署和 API
- `app-design/PROJECT_SUMMARY.md`：移动端与网页整体结构
- `app-design/docs/01-app-entry.md` - `07-widget.md`：分层说明

## License

MIT
