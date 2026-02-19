# StackDo (Life App)

> **"Push to Start, Pop to Finish."**

[中文文档 (Chinese README)](README_CN.md)

A task management app based on the **Push/Pop stack concept** - treat your daily tasks like a computer stack.

## ✨ Features

### Core Functions
- **Push** - Quickly capture tasks with title, deadline, and priority
- **Pop** - Swipe right to complete tasks with satisfying gesture interaction
- **Queue View** - List all active tasks sorted by deadline
- **Timeline View** - Visualize tasks on a time-based flow

### Advanced Features
- **Archive** - View completed task history with search and tag filtering
- **Templates** - Quick-start with preset task templates (Work, Study, Exercise, etc.)
- **Statistics** - Track completion rate, daily/weekly progress
- **Widget** - Home screen widget showing current task
- **Notifications** - DDL reminders and daily summary
- **Tags** - Organize tasks with custom tags
- **Theme** - Support for Light/Dark/System themes
- **Localization** - English and Chinese language support

### Sync & Cloud
- **Server Sync** - RESTful API with password-based authentication
- **Public Feed** - Publish and display personal status/posts on the web
- **Config-Driven Web** - Personal text/links are managed via `Server/public/config.json`
- **Cloudflare Runtime** - Worker + D1 powers APIs and public web pages

## 🛠 Tech Stack

| Category | Technology |
|----------|------------|
| **UI** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM + Repository Pattern |
| **Database** | Room |
| **Network** | Retrofit + OkHttp |
| **Background** | WorkManager |
| **Widget** | Glance |
| **Language** | Kotlin 2.0 |
| **Min SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 16 (API 35) |

## 📱 Screenshots

*Coming soon*

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17

### Build

```bash
# Clone the repository
git clone https://github.com/mico-v/life-app.git

# Navigate to project
cd life-app

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test
```

### Install
```bash
# Install on connected device
./gradlew installDebug
```

## 📖 Documentation

For detailed project documentation, see:

- **[Project Summary](app-design/PROJECT_SUMMARY.md)** - Complete project overview
- **[App Entry & Navigation](app-design/docs/01-app-entry.md)** - Application entry points
- **[Data Layer](app-design/docs/02-data-layer.md)** - Room database and entities
- **[Network Layer](app-design/docs/03-network-layer.md)** - API and sync
- **[ViewModel Layer](app-design/docs/04-viewmodel-layer.md)** - State management
- **[UI Layer](app-design/docs/05-ui-layer.md)** - Compose UI components
- **[Workers](app-design/docs/06-workers.md)** - Background tasks
- **[Widget](app-design/docs/07-widget.md)** - Home screen widget
- **[Worker Deployment](Server/worker/README.md)** - Cloudflare Worker + D1 deployment guide

## 🌐 Backend Deployment

Backend APIs and public pages are deployed as a Cloudflare Worker:
- Worker source: `Server/worker`
- Static assets: `Server/public`
- Database: Cloudflare D1 (`DB` binding)
- CI deploy workflow: `.github/workflows/worker_deploy.yml`

## 📁 Project Structure

```
app/src/main/java/com/example/android16demo/
├── LifeApp.kt              # Application class
├── MainActivity.kt         # Main Activity & Navigation
├── data/                   # Data layer
│   ├── entity/             # Room entities
│   ├── dao/                # Data Access Objects
│   ├── repository/         # Repositories
│   └── sync/               # Sync preferences
├── network/                # Network layer
│   ├── api/                # Retrofit API
│   └── model/              # DTOs
├── ui/                     # UI layer
│   ├── components/         # Reusable Composables
│   ├── screen/             # Screen Composables
│   └── theme/              # Material 3 Theme
├── viewmodel/              # ViewModels
├── widget/                 # Glance Widget
└── worker/                 # WorkManager workers
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)

---

*Built with ❤️ using Kotlin and Jetpack Compose*
