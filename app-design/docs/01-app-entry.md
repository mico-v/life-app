# 01 - App Entry & Navigation

## Entry

- File: `app/src/main/java/com/example/android16demo/LifeApp.kt`
- Responsibility:
  - Provide `SyncPreferences`
  - Provide `WebRepository`

## Activity & Navigation

- File: `app/src/main/java/com/example/android16demo/MainActivity.kt`
- Single-activity Compose app.
- Bottom navigation contains 3 routes:
  - `status`
  - `profile`
  - `publish`

## Screen Mapping

- `status` -> `StatusScreen`
- `profile` -> `WebProfileScreen`
- `publish` -> `PublishScreen`

Each screen uses a dedicated ViewModel created with a small local `ViewModelProvider.Factory`.

## Locale & Theme

- Locale is applied in `attachBaseContext()` via `LocaleHelper`.
- Theme mode is read from `SyncPreferences` and passed to `Android16DemoTheme`.
