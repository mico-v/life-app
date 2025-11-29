# App Design: Life App (Push/Pop Task Manager)

## 1. Product Philosophy
**"Push to Start, Pop to Finish."**
Treat life tasks as a computing stack/queue.
- **Push**: Rapidly capture tasks.
- **Queue**: Visualize workload on a timeline.
- **Pop**: Complete and archive tasks with satisfaction.
- **Broadcast**: Share your availability (Busy/Free) with the world.

## 2. Core Features

### A. Mobile App (Android)
1.  **The Queue (Home Screen)**
    - **Timeline View**: Visual representation of tasks sorted by time/DDL.
    - **Status Indicators**: Color-coded by urgency or progress.
    - **Quick Actions**: Swipe to "Pop" (Complete), Tap to expand.

2.  **Push (Task Creation)**
    - **Quick Entry**: Title, optional DDL.
    - **Detailed Mode**:
        - Content/Description.
        - Time Range (Start - End).
        - References (Links, Text snippets).
        - **Templates**: Pre-sets for "Work", "Study", "Workout" (sets default duration/icon).

3.  **Pop (Completion & Archive)**
    - **Action**: Satisfying animation when completing a task.
    - **Archive**: History of "Popped" tasks.
    - **Progress Tracking**: Slider for 0-100% on active tasks.

4.  **Personal Center**
    - **Stats**: "Tasks Popped this week", "Focus Hours".
    - **Profile**: User info.

5.  **Server Sync (Broadcast)**
    - **Manual/Auto Sync**: Push local state to the server.
    - **Privacy Control**: Select which tasks are "Public" vs "Private".

### B. Web Status Board (Server)
1.  **Public Profile**: `life-app.com/u/username`
2.  **Status Indicator**:
    - **BUSY**: If currently within a task time range.
    - **FREE**: If no active tasks now.
3.  **Timeline View**: Read-only view of public tasks for the day/week.

## 3. Technical Architecture

### Android Client
- **UI**: Jetpack Compose (Material 3).
- **Architecture**: MVVM (Model-View-ViewModel).
- **Local Data**: Room Database (Source of Truth).
- **Networking**: Retrofit (Sync to Server).
- **Background Work**: WorkManager (for DDL notifications and background sync).

### Backend (Server) - *To Be Developed*
- **Tech Stack**: Lightweight (e.g., Python FastAPI or Node.js Express).
- **Database**: Simple SQL (SQLite/PostgreSQL).
- **API**:
    - `POST /sync`: Receive tasks/status from App.
    - `GET /status/{user}`: Public view data.
- **Frontend**: Simple React or static HTML/JS page.

## 4. Data Model (Draft)

### Task Entity
```kotlin
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String?,
    val createdAt: Long,
    val startTime: Long?,
    val deadline: Long?,
    val isCompleted: Boolean, // Popped?
    val progress: Float, // 0.0 - 1.0
    val isPublic: Boolean, // Visible on web?
    val templateId: String? // Link to a template
)
```

## 5. UI/UX Flows
- **Main Screen**:
    - Bottom Bar: Queue | Profile.
    - FAB: "Push" (Add Task).
- **Push Flow**: Click FAB -> BottomSheet or Full Screen -> Enter details -> Save -> Animates into Timeline.
- **Pop Flow**: Swipe Right on Task -> "Popped!" animation -> Moves to Archive.

## 6. Development Phases
- **Phase 1 (MVP)**: Local Push/Pop, List View, Room DB.
- **Phase 2 (Visuals)**: Timeline UI, DDL Reminders, Progress.
- **Phase 3 (Web)**: Server setup, Sync logic, Public Status Page.
- **Phase 4 (Polish)**: Templates, Stats, Animations.
