# 05 - UI Layer

## Tech

- Jetpack Compose + Material 3
- Theme files:
  - `app/src/main/java/com/example/android16demo/ui/theme/Theme.kt`
  - `app/src/main/java/com/example/android16demo/ui/theme/Color.kt`
  - `app/src/main/java/com/example/android16demo/ui/theme/Type.kt`

## Screens

### `StatusScreen`
- File: `app/src/main/java/com/example/android16demo/ui/screen/StatusScreen.kt`
- Displays public feed:
  - primary status card
  - source status list
  - latest posts

### `WebProfileScreen`
- File: `app/src/main/java/com/example/android16demo/ui/screen/WebProfileScreen.kt`
- Edits local profile presentation settings.

### `PublishScreen`
- File: `app/src/main/java/com/example/android16demo/ui/screen/PublishScreen.kt`
- Publish status/post and manage post list.

## Navigation UI

Bottom navigation is defined in `MainActivity.kt` and matches the three screens:
- Status
- Profile
- Publish
