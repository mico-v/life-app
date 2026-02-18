# 03 - 网络层 (Network Layer)

## 概述

网络层负责与后端服务器通信，实现任务同步与公开状态拉取。当前实现基于 **Retrofit + OkHttp**，并与 Cloudflare Worker（Hono + D1）接口契合。

---

## API 接口定义

### LifeAppApi.kt

**路径:** `network/api/LifeAppApi.kt`

> ⚠️ `BASE_URL` 是占位符。运行时通过 `SyncPreferences.serverUrl` 传入实际地址，再由 `RetrofitClient.getApi(baseUrl)` 创建实例。

当前主要接口：

```kotlin
interface LifeAppApi {
    companion object {
        const val BASE_URL = "https://api.life-app.com/"
        const val API_VERSION = "v1"
    }

    @POST("api/$API_VERSION/sync")
    suspend fun syncTasks(
        @Header("x-client-token") clientToken: String,
        @Header("x-server-password") serverPassword: String,
        @Body request: SyncRequest
    ): Response<SyncResponse>

    @GET("api/$API_VERSION/tasks")
    suspend fun getTasks(
        @Header("x-client-token") clientToken: String,
        @Header("x-server-password") serverPassword: String
    ): Response<Map<String, List<TaskDto>>>

    @GET("api/$API_VERSION/public/dashboard")
    suspend fun getPublicDashboard(): Response<DashboardResponse>

    @GET("api/$API_VERSION/u/{username}/status")
    suspend fun getUserStatus(@Path("username") username: String): Response<UserStatusResponse>

    @PUT("api/$API_VERSION/profile")
    suspend fun updateProfile(
        @Header("x-client-token") clientToken: String,
        @Header("x-server-password") serverPassword: String,
        @Body profile: Map<String, String>
    ): Response<Map<String, Any>>
}
```

> 说明：`login/register` 仍在接口中保留为 `@Deprecated` 兼容占位方法，当前同步流程不依赖它们。

---

## DTO 与模型

### ApiModels.kt

**路径:** `network/model/ApiModels.kt`

- `TaskDto`: 任务同步对象（含 `tags`）
- `SyncRequest`: 同步请求体（`user_id + tasks + last_sync`）
- `SyncResponse`: 同步返回（`success/message/server_time/updated_tasks`）
- `DashboardResponse`: 公开看板数据
- `UserStatusResponse`: 用户公开状态
- `AuthRequest/AuthResponse`: 历史认证模型（当前主链路未使用）

---

## Retrofit 客户端

### RetrofitClient.kt

**路径:** `network/RetrofitClient.kt`

实际代码通过 `getApi(baseUrl: String)` 按需构建 Retrofit 实例，不依赖全局可变 Base URL。每次同步会读取 `SyncPreferences.serverUrl`，确保可从设置页动态切换服务端地址。

---

## 同步仓库

### SyncRepository.kt

**路径:** `data/sync/SyncRepository.kt`

`SyncRepository` 负责把本地 Task 与服务端进行双向同步。

#### 同步结果类型

```kotlin
sealed class SyncResult {
    data class Success(val syncedCount: Int, val serverTime: Long) : SyncResult()
    data class Error(val message: String) : SyncResult()
    data object NotConfigured : SyncResult()
}
```

#### 当前配置判断逻辑

- 不再使用“登录态”作为同步前置条件。
- 仅要求 `SyncPreferences.isSyncConfigured == true`（即 `serverUrl` 与 `serverPassword` 已配置）。
- `clientToken` 在本地自动生成并持久化。

#### 关键流程

1. 读取本地任务并转换为 `TaskDto`
2. 构造 `SyncRequest`
3. 使用 `x-client-token + x-server-password` 头调用 `/api/v1/sync`
4. 将服务端返回的 `updatedTasks` 回写数据库
5. 更新 `lastSyncTime`

---

## 与服务端契约对齐说明

当前客户端文档已与仓库内服务端方案一致：

- 部署位置：`Server/worker`
- 运行时：Cloudflare Worker
- 框架：Hono
- 数据库：Cloudflare D1
- 鉴权方式：`x-server-password` + 客户端 token

详见：`Server/worker/README.md`
