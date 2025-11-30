# 07 - 桌面组件 (Widget)

## 概述

桌面小组件使用 Jetpack Glance 库实现，显示当前任务和待办数量。

---

## LifeAppWidget

### LifeAppWidget.kt

**路径:** `widget/LifeAppWidget.kt`

#### GlanceAppWidget 实现

```kotlin
class LifeAppWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 获取 Application 实例 (可能为 null 如果 Context 不是 LifeApp)
        val app = context.applicationContext as? LifeApp
        val repository = app?.taskRepository
        
        // 获取活跃任务
        // 注意: 使用 ?: emptyList() 处理以下情况:
        // - repository 为 null (app 转换失败)
        // - getActiveTasks() 返回空 Flow
        // - Flow.first() 抛出异常
        // 在这些情况下，Widget 将显示 "No active tasks"
        val activeTasks = repository?.getActiveTasks()?.first() ?: emptyList()
        val currentTask = activeTasks.firstOrNull()
        val todayTaskCount = activeTasks.size
        
        provideContent {
            GlanceTheme {
                WidgetContent(
                    currentTask = currentTask,
                    todayTaskCount = todayTaskCount
                )
            }
        }
    }
}
```

#### Widget 内容

```kotlin
@Composable
private fun WidgetContent(
    currentTask: Task?,
    todayTaskCount: Int
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // 标题
        Text(
            text = "Life App",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GlanceTheme.colors.primary
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // 当前任务
        if (currentTask != null) {
            Text(
                text = "Current Task",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = currentTask.title,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onBackground
                ),
                maxLines = 2
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // 进度
            val progressPercent = (currentTask.progress * 100).toInt()
            Text(
                text = "Progress: $progressPercent%",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.primary
                )
            )
        } else {
            Text(
                text = "No active tasks",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = "Tap to add a task",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.primary
                )
            )
        }
        
        // 弹性空间
        Spacer(modifier = GlanceModifier.defaultWeight())
        
        // 底部任务数量
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$todayTaskCount tasks remaining",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}
```

---

## Widget Receiver

### GlanceAppWidgetReceiver

```kotlin
class LifeAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LifeAppWidget()
}
```

---

## 配置文件

### AndroidManifest.xml

```xml
<application>
    <!-- Widget Receiver -->
    <receiver
        android:name=".widget.LifeAppWidgetReceiver"
        android:exported="true">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        </intent-filter>
        <meta-data
            android:name="android.appwidget.provider"
            android:resource="@xml/life_app_widget_info" />
    </receiver>
</application>
```

### life_app_widget_info.xml

**路径:** `res/xml/life_app_widget_info.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:targetCellWidth="3"
    android:targetCellHeight="2"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen" />
```

#### 配置说明

| 属性 | 说明 |
|------|------|
| `minWidth` / `minHeight` | 最小尺寸 |
| `targetCellWidth` / `targetCellHeight` | 目标网格单元数 |
| `updatePeriodMillis` | 自动更新间隔 (30分钟) |
| `initialLayout` | 初始加载布局 |
| `resizeMode` | 可调整大小方向 |
| `widgetCategory` | 组件类别 |

---

## Glance API 要点

### Glance vs Compose

Glance 使用类似 Compose 的语法，但有以下区别：

| Compose | Glance |
|---------|--------|
| `Modifier` | `GlanceModifier` |
| `Column` | `androidx.glance.layout.Column` |
| `Text` | `androidx.glance.text.Text` |
| `MaterialTheme` | `GlanceTheme` |

### 常用修饰符

```kotlin
GlanceModifier
    .fillMaxSize()
    .fillMaxWidth()
    .padding(16.dp)
    .background(color)
    .size(width, height)
    .width(dp)
    .height(dp)
    .defaultWeight()  // 类似 weight(1f)
```

### 主题颜色

```kotlin
GlanceTheme.colors.background
GlanceTheme.colors.primary
GlanceTheme.colors.onBackground
GlanceTheme.colors.onSurfaceVariant
GlanceTheme.colors.surface
```

### 文本样式

```kotlin
Text(
    text = "Hello",
    style = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = GlanceTheme.colors.primary
    ),
    maxLines = 2
)
```

---

## 手动更新 Widget

```kotlin
import androidx.glance.appwidget.updateAll

// 在数据变更后更新所有 Widget 实例
suspend fun updateWidgets(context: Context) {
    LifeAppWidget().updateAll(context)
}
```

### 使用示例

```kotlin
// 在 ViewModel 或 Repository 中
fun popTask(taskId: String) {
    viewModelScope.launch {
        repository.popTask(taskId)
        
        // 更新 Widget
        LifeAppWidget().updateAll(context)
    }
}
```

---

## Widget 布局

```
┌─────────────────────────────────────┐
│ Life App                            │
│                                     │
│ Current Task                        │
│ ┌─────────────────────────────────┐ │
│ │ Complete project documentation  │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Progress: 60%                       │
│                                     │
│                  5 tasks remaining  │
└─────────────────────────────────────┘
```

---

## 添加点击行为

```kotlin
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity

@Composable
private fun WidgetContent(...) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity<MainActivity>())  // 点击打开应用
    ) {
        // ...
    }
}
```

---

## 依赖配置

### gradle/libs.versions.toml

```toml
[versions]
glance = "1.1.0"

[libraries]
androidx-glance = { group = "androidx.glance", name = "glance", version.ref = "glance" }
androidx-glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
```

### app/build.gradle.kts

```kotlin
dependencies {
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
}
```

---

## 相关文件

- `widget/LifeAppWidget.kt` - Widget 实现
- `res/xml/life_app_widget_info.xml` - Widget 配置
- `AndroidManifest.xml` - Receiver 注册
