# Copilot Instructions for life-app

This is an Android project built with Kotlin and Jetpack Compose. Follow these guidelines to ensure code consistency and effective integration.

## 1. Project Architecture & Tech Stack

- **Type**: Native Android Application (Single Activity).
- **Language**: Kotlin (exclusively).
- **UI Framework**: Jetpack Compose (Material 3). **Do not use XML layouts** for UI unless absolutely necessary (e.g., complex drawables).
- **Build System**: Gradle with Kotlin DSL (`.kts`).
- **Dependency Management**: Version Catalog (`gradle/libs.versions.toml`).

## 2. Key Conventions & Patterns

### UI & Navigation
- **Screens**: Define screens as top-level Composable functions (e.g., `HomeScreen`, `SettingsScreen`).
- **Navigation**: Use `androidx.navigation.compose`.
  - Define routes using a sealed class (e.g., `sealed class Screen(val route: String)`).
  - Host navigation in `MainActivity` or a dedicated `AppNavigation` composable.
- **Theming**: Use Material 3. Support dynamic colors (Android 12+) via `dynamicDarkColorScheme` / `dynamicLightColorScheme`.
- **Previews**: Always include `@Preview` composables for UI components to facilitate design iteration.

### Dependency Management
- **Adding Libraries**:
  1.  Define the version in `[versions]` block of `gradle/libs.versions.toml`.
  2.  Define the library in `[libraries]` block of `gradle/libs.versions.toml`.
  3.  Reference the library in `app/build.gradle.kts` using `libs.name.of.lib`.
  - **Do not** hardcode versions or dependencies directly in `build.gradle.kts`.

### Code Style
- **Kotlin**: Follow official Kotlin coding conventions.
- **Composables**: Use PascalCase for Composable functions. Functions that return `Unit` should be named as nouns/adjectives (e.g., `MainScreen`).
- **State Management**: Use `remember` and `mutableStateOf` for local state. Hoist state to callers when possible.

## 3. Critical Files & Directories

- `app/src/main/java/com/example/android16demo/MainActivity.kt`: Entry point, Navigation Host, and main UI scaffold.
- `gradle/libs.versions.toml`: Central source of truth for dependencies and versions.
- `app/build.gradle.kts`: Module-level build configuration.
- `app/src/main/java/com/example/android16demo/ui/theme/`: Theme definitions (Color, Type, Theme).

## 4. Build & Test Commands

- **Build Debug APK**: `./gradlew assembleDebug`
- **Run Unit Tests**: `./gradlew test`
- **Run Instrumented Tests**: `./gradlew connectedAndroidTest`
- **Lint Check**: `./gradlew lint`

## 5. Common Tasks

- **New Screen**: Create a new Composable file, add a route to the `Screen` sealed class, and add a `composable()` entry in the `NavHost`.
- **New Dependency**: Update `libs.versions.toml` first, then sync Gradle.
