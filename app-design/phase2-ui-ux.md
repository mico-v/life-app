# 阶段 2: 视觉与交互进阶 (Visuals & Interaction)

## 目标
将基础的列表视图升级为核心的 "Timeline" (时间轴) 视图，并引入 "Push/Pop" 的手势交互和动画，提升用户体验。

## 1. 时间轴视图 (Timeline View)

### 1.1 概念
不再是简单的垂直列表，而是基于时间的线性流。
- **过去**: 灰色/淡化，显示未完成的逾期任务。
- **现在**: 高亮，显示当前时间段的任务。
- **未来**: 按时间顺序排列。

### 1.2 UI 实现
- **自定义 Layout**: 可能需要使用 `Layout` composable 自定义布局，或者高度定制的 `LazyColumn`。
- **时间刻度**: 在左侧显示时间轴线 (Timeline Indicator)。
- **任务卡片**: 根据任务时长 (Start - End) 动态计算卡片高度（可选，或保持固定高度但按时间分组）。

## 2. 交互设计 (Gestures)

### 2.1 Swipe to Pop (滑动完成)
- 使用 `SwipeToDismiss` 或自定义 `anchoredDraggable`。
- **右滑**: "Pop" (完成)。显示绿色背景和对勾图标。
- **左滑**: "Delete" (删除) 或 "Archive" (归档)。
- **动画**: 任务完成时，卡片缩小并飞向归档图标 (Hero Animation)。

### 2.2 进度控制
- 在任务卡片上直接滑动或点击进度条来更新 `progress` 字段。

## 3. 通知与提醒 (Notifications)

### 3.1 DDL 提醒
- 使用 `WorkManager` 或 `AlarmManager`。
- 在任务 `deadline` 前 15分钟/1小时 发送本地通知。

### 3.2 每日摘要
- 每天早晨推送今日待办概览。

## 4. 开发步骤 Prompt 指引

1.  **滑动交互**: "请为 `TaskItem` 添加滑动删除/完成功能，使用 Compose 的 `SwipeToDismissBox` (Material3)。"
2.  **时间轴 UI**: "请设计一个 Timeline 组件，左侧是时间线，右侧是任务卡片，按时间顺序排列。"
3.  **动画效果**: "请为任务完成添加一个过渡动画，当任务状态变为 completed 时，让它淡出并从列表中移除。"
4.  **提醒功能**: "请集成 WorkManager，当创建一个带有 DDL 的任务时，调度一个一次性 Worker 发送通知。"
