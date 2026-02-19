# Copilot Instructions for life-app

## 1. Product Direction

This repo is a **status-first personal project**.
Do not generate new task-management flows unless explicitly requested.

Primary app surfaces are:
- `Status`
- `Profile`
- `Publish`

## 2. Stack

- Android app: Kotlin + Jetpack Compose + Material 3
- Backend: Cloudflare Worker + D1
- Build: Gradle Kotlin DSL + version catalog

## 3. Coding Conventions

### UI & Navigation
- Use `androidx.navigation.compose`.
- Keep routes aligned with `MainActivity.kt`.
- Prefer Compose-only UI (no XML layout additions unless required).

### Dependencies
- Add versions and libraries through `gradle/libs.versions.toml`.
- Do not hardcode dependency versions in `app/build.gradle.kts`.

### State
- Local UI state: Compose state/StateFlow
- Network/domain state: ViewModel + repository

## 4. Critical Paths

- App entry/navigation:
  - `app/src/main/java/com/example/android16demo/LifeApp.kt`
  - `app/src/main/java/com/example/android16demo/MainActivity.kt`
- Data/network:
  - `app/src/main/java/com/example/android16demo/data/repository/WebRepository.kt`
  - `app/src/main/java/com/example/android16demo/network/api/LifeAppApi.kt`
  - `app/src/main/java/com/example/android16demo/network/model/ApiModels.kt`
- Web/worker:
  - `Server/public/*`
  - `Server/worker/src/index.js`

## 5. Build/Test Commands

```bash
./gradlew :app:assembleDebug
./gradlew test
./gradlew lint
```

Worker local:

```bash
cd Server/worker
pnpm run db:migrate:local
pnpm run dev
```
