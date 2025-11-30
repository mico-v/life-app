# 03 - 网络层 (Network Layer)

## 概述

网络层负责与后端服务器通信，实现数据同步功能。使用 Retrofit + OkHttp 构建。

---

## API 接口定义

### LifeAppApi.kt

**路径:** `network/api/LifeAppApi.kt`

> ⚠️ **配置说明:** `BASE_URL` 是占位符，部署前需要在 `RetrofitClient.kt` 中通过 `setBaseUrl()` 配置实际的生产服务器地址。

```kotlin
interface LifeAppApi {
    
    companion object {
        // 占位符 URL - 实际部署时需要配置
        // 可通过 RetrofitClient.setBaseUrl() 动态更新
        const val BASE_URL = "https://api.life-app.com/"
        const val API_VERSION = "v1"
    }
    
    // 同步任务数据
    @POST("api/$API_VERSION/sync")
    suspend fun syncTasks(
        @Header("Authorization") token: String,
        @Body request: SyncRequest
    ): Response<SyncResponse>
    
    // 获取用户公开状态
    @GET("api/$API_VERSION/u/{username}/status")
    suspend fun getUserStatus(
        @Path("username") username: String
    ): Response<UserStatusResponse>
    
    // 登录
    @POST("api/$API_VERSION/auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>
    
    // 注册
    @POST("api/$API_VERSION/auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): Response<AuthResponse>
    
    // 登出
    @POST("api/$API_VERSION/auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>
}
```

---

## 数据传输对象 (DTOs)

### ApiModels.kt

**路径:** `network/model/ApiModels.kt`

#### TaskDto - 任务传输对象
```kotlin
data class TaskDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("start_time") val startTime: Long?,
    @SerializedName("deadline") val deadline: Long?,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("completed_at") val completedAt: Long?,
    @SerializedName("progress") val progress: Float,
    @SerializedName("priority") val priority: Int,
    @SerializedName("is_public") val isPublic: Boolean
)
```

#### SyncRequest - 同步请求
```kotlin
data class SyncRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("tasks") val tasks: List<TaskDto>,
    @SerializedName("last_sync") val lastSync: Long?
)
```

#### SyncResponse - 同步响应
```kotlin
data class SyncResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("server_time") val serverTime: Long,
    @SerializedName("updated_tasks") val updatedTasks: List<TaskDto>?
)
```

#### AuthRequest / AuthResponse - 认证
```kotlin
data class AuthRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String?,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("error") val error: String?
)
```

#### UserStatusResponse - 用户状态
```kotlin
data class UserStatusResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("status") val status: String,     // "BUSY" or "FREE"
    @SerializedName("current_task") val currentTask: String?,
    @SerializedName("public_tasks") val publicTasks: List<TaskDto>,
    @SerializedName("stats") val stats: UserStats?
)
```

---

## Retrofit 客户端配置

### RetrofitClient.kt

**路径:** `network/RetrofitClient.kt`

```kotlin
object RetrofitClient {
    
    private var baseUrl: String = LifeAppApi.BASE_URL
    
    // 日志拦截器
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // OkHttp 客户端配置
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Retrofit 实例
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // API 实例
    val api: LifeAppApi by lazy {
        retrofit.create(LifeAppApi::class.java)
    }
    
    // 动态更新 Base URL
    fun setBaseUrl(url: String) {
        baseUrl = url
    }
}
```

---

## 同步仓库

### SyncRepository.kt

**路径:** `data/sync/SyncRepository.kt`

负责处理数据同步逻辑。

#### 同步结果类型
```kotlin
sealed class SyncResult {
    data class Success(val syncedCount: Int, val serverTime: Long) : SyncResult()
    data class Error(val message: String) : SyncResult()
    data object NotLoggedIn : SyncResult()
}
```

#### 核心同步方法
```kotlin
suspend fun syncTasks(): SyncResult {
    if (!syncPreferences.isLoggedIn) {
        return SyncResult.NotLoggedIn
    }
    
    val token = syncPreferences.authToken ?: return SyncResult.NotLoggedIn
    val userId = syncPreferences.userId ?: return SyncResult.NotLoggedIn
    
    try {
        // 获取本地任务
        val localTasks = taskRepository.getAllTasks().first()
        
        // 转换为 DTO
        val taskDtos = localTasks.map { it.toDto() }
        
        // 创建请求
        val request = SyncRequest(
            userId = userId,
            tasks = taskDtos,
            lastSync = syncPreferences.lastSyncTime.takeIf { it > 0 }
        )
        
        // 发送到服务器
        val response = api.syncTasks("Bearer $token", request)
        
        if (response.isSuccessful && response.body()?.success == true) {
            // 更新本地数据库
            response.body()?.updatedTasks?.forEach { dto ->
                taskRepository.updateTask(dto.toEntity())
            }
            
            // 更新最后同步时间
            syncPreferences.lastSyncTime = response.body()!!.serverTime
            
            return SyncResult.Success(taskDtos.size, response.body()!!.serverTime)
        }
        
        return SyncResult.Error(response.body()?.message ?: "Sync failed")
    } catch (e: Exception) {
        return SyncResult.Error(e.message ?: "Unknown error")
    }
}
```

#### 认证方法
```kotlin
// 登录
suspend fun login(username: String, password: String): Result<String>

// 注册
suspend fun register(username: String, password: String): Result<String>

// 登出
suspend fun logout(): Result<Unit>
```

#### Entity ↔ DTO 转换

```kotlin
// Task -> TaskDto
private fun Task.toDto(): TaskDto = TaskDto(
    id = id,
    title = title,
    description = description,
    createdAt = createdAt,
    startTime = startTime,
    deadline = deadline,
    isCompleted = isCompleted,
    completedAt = completedAt,
    progress = progress,
    priority = priority,
    isPublic = isPublic
)

// TaskDto -> Task
private fun TaskDto.toEntity(): Task = Task(
    id = id,
    title = title,
    description = description,
    createdAt = createdAt,
    startTime = startTime,
    deadline = deadline,
    isCompleted = isCompleted,
    completedAt = completedAt,
    progress = progress,
    priority = priority,
    isPublic = isPublic
)
```

---

## 安全存储

### SyncPreferences.kt

**路径:** `data/sync/SyncPreferences.kt`

使用 EncryptedSharedPreferences 安全存储敏感数据。

```kotlin
class SyncPreferences(context: Context) {
    
    private val prefs: SharedPreferences
    
    init {
        // 创建加密主密钥
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        // 创建加密 SharedPreferences
        prefs = EncryptedSharedPreferences.create(
            context,
            "life_app_sync_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    // 存储的数据
    var authToken: String?          // 认证令牌
    var userId: String?             // 用户 ID
    var username: String?           // 用户名
    var lastSyncTime: Long          // 最后同步时间
    var autoSyncEnabled: Boolean    // 是否启用自动同步
    var syncOnWifiOnly: Boolean     // 仅在 WiFi 下同步
    
    // 检查登录状态
    val isLoggedIn: Boolean
        get() = !authToken.isNullOrEmpty() && !userId.isNullOrEmpty()
    
    // 清除认证信息
    fun clearAuth()
    
    // 清除所有数据
    fun clearAll()
}
```

---

## 同步流程图

```
┌─────────────────┐
│   用户触发同步   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  检查登录状态    │──── 未登录 ────▶ 返回 NotLoggedIn
└────────┬────────┘
         │ 已登录
         ▼
┌─────────────────┐
│ 获取本地任务数据 │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 转换为 TaskDto  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 发送 POST /sync │
└────────┬────────┘
         │
         ▼
    ┌────┴────┐
    │ 成功？  │
    └────┬────┘
      ╱     ╲
   是 ▼       ▼ 否
┌─────────┐  ┌─────────┐
│更新本地 │  │返回Error│
│数据库   │  └─────────┘
└────┬────┘
     │
     ▼
┌─────────────────┐
│ 更新lastSyncTime│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  返回 Success   │
└─────────────────┘
```

---

## 后端 API 要求 (For Backend Implementation)

### POST /api/v1/sync
```json
// Request
{
    "user_id": "uuid",
    "tasks": [TaskDto],
    "last_sync": 1234567890  // 可选
}

// Response
{
    "success": true,
    "message": null,
    "server_time": 1234567890,
    "updated_tasks": [TaskDto]  // 服务器端有更新时返回
}
```

### POST /api/v1/auth/login
```json
// Request
{
    "username": "user",
    "password": "pass"
}

// Response
{
    "success": true,
    "token": "jwt_token",
    "user_id": "uuid",
    "username": "user",
    "error": null
}
```

---

## 相关文件

- `network/api/LifeAppApi.kt` - API 接口
- `network/model/ApiModels.kt` - DTOs
- `network/RetrofitClient.kt` - 客户端配置
- `data/sync/SyncRepository.kt` - 同步仓库
- `data/sync/SyncPreferences.kt` - 安全存储
- `worker/SyncWorker.kt` - 后台同步
